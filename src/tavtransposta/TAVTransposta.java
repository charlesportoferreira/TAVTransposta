/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tavtransposta;

//import convertclustertotav.ConvertClusterToTAV;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import transpor_arff.CreateTAV;

/**
 *
 * @author charleshenriqueportoferreira
 */
public class TAVTransposta {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        CreateTAV ct = new CreateTAV();
        int numAtributos = 0;
        int clusterMenorError = 0;

        try {
            System.out.println("limpando dados");
            ct.limpaDados("discover.data", "discover.txt");                        //limpa as TAV

            System.out.println("extraindo classes");
            numAtributos = ct.getClasses("discover.names", "classes.txt");    //extrai as classes
            System.out.println(numAtributos);
            if (args.length != 0) {
                numAtributos = Integer.parseInt(args[0]);
            } else {
                numAtributos = (int) Math.sqrt(numAtributos);
//            numAtributos = (int) numAtributos / 2;
//            numAtributos = numAtributos * 20 / 100;//20% da base de dados
            }
            String nAtributos = String.valueOf(numAtributos);
            System.out.println(nAtributos);

            System.out.println("transpondo a matriz");
            Process p = Runtime.getRuntime().exec("Rscript scriptTransposicao.r");  //transpoe a TAV
            p.waitFor();                                                            //aguarda fim do processo

            System.out.println("Reduzindo a tabela");
            ct.reduzArff("Tdiscover.txt", "TRdiscover.txt");

            System.out.println("criando arff com TAV transposta");
            ct.unirDados("TRdiscover.txt", "discover.arff");
            double menorErro = 100000000;
            double erroAtual = 0;
            //cria arff com a tav transposta
            for (int i = 0; i < 10; i++) {
                String seed = String.valueOf(i);
                System.out.println("fazendo a clusterizacao: " + i);
                String[] cm2 = {"java", "-cp", "weka.jar", "weka.clusterers.SimpleKMeans", "-N", nAtributos, "-I", "500", "-O", "-S", seed, "-t", "discover.arff"};
                ProcessBuilder builder = new ProcessBuilder(Arrays.asList(cm2));
                builder.redirectOutput(new File("cluster" + i + ".txt"));
                builder.redirectError(new File("error" + i + ".txt"));
                Process p2 = builder.start(); // throws IOException
                p2.waitFor();
                erroAtual = lerErroAgrupamento("cluster" + i + ".txt");
                if (erroAtual < menorErro) {
                    menorErro = erroAtual;
                    clusterMenorError = i;
                }
                System.out.println(erroAtual);
            }

            for (int i = 0; i < 10; i++) {
                if (i == clusterMenorError) {
                    continue;
                }
                Runtime.getRuntime().exec("rm cluster" + i + ".txt");
                Runtime.getRuntime().exec("rm error" + i + ".txt");
            }

            System.out.println("Menor erro: " + menorErro);
            System.out.println("excluindo arquivos");
            Runtime.getRuntime().exec("mv cluster" + clusterMenorError + ".txt cluster.txt");
            System.out.println("Comando: " + "mv cluster" + clusterMenorError + ".txt cluster.txt");
            Runtime.getRuntime().exec("rm discover.txt");
            Runtime.getRuntime().exec("rm Tdiscover.txt");
            Runtime.getRuntime().exec("rm TRdiscover.txt");
            Runtime.getRuntime().exec("rm discover.arff");

        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(TAVTransposta.class.getName()).log(Level.SEVERE, null, ex);
        }
//        }
    }

    public static double lerErroAgrupamento(String sourceFile) throws FileNotFoundException, IOException {
        String linha;
        double error = 0;
        try (FileReader fr = new FileReader(sourceFile); BufferedReader br = new BufferedReader(fr)) {
            while (br.ready()) {
                linha = br.readLine();
                if (linha.contains("Within cluster sum of squared errors:")) {
                    linha = linha.split(":")[1];
                    error = Double.parseDouble(linha);
                    break;
                }
            }
            br.close();
            fr.close();
        }
        return error;
    }
}
