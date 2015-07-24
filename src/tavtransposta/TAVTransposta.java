/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tavtransposta;

//import convertclustertotav.ConvertClusterToTAV;
import java.io.File;
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
        try {
            System.out.println("limpando dados");
            ct.limpaDados("discover.data", "discover.txt");                        //limpa as TAV

            System.out.println("extraindo classes");
            ct.getClasses("discover.names", "classes.txt");                        //extrai as classes

            System.out.println("transpondo a matriz");
            Process p = Runtime.getRuntime().exec("Rscript scriptTransposicao.r");  //transpoe a TAV
            p.waitFor();                                                            //aguarda fim do processo

           ct.reduzArff("Tdiscover.txt", "TRdiscover.txt");

            System.out.println("criando arff com TAV transposta");
            ct.unirDados("TRdiscover.txt", "discover.arff");                         //cria arff com a tav transposta

            System.out.println("fazendo a clusterizacao");
            String[] cm2 = {"java", "-cp", "weka.jar", "weka.clusterers.SimpleKMeans", "-N", "10", "-I", "500", "-O", "-S", "10", "-t", "discover.arff"};
            ProcessBuilder builder = new ProcessBuilder(Arrays.asList(cm2));
            builder.redirectOutput(new File("cluster.txt"));
            builder.redirectError(new File("error.txt"));
            Process p2 = builder.start(); // throws IOException
            p2.waitFor();

            System.out.println("excluindo arquivos");
            Runtime.getRuntime().exec("rm discover.txt");
            Runtime.getRuntime().exec("rm Tdiscover.txt");
            Runtime.getRuntime().exec("rm TRdiscover.txt");
            Runtime.getRuntime().exec("rm discover.arff");

        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(TAVTransposta.class.getName()).log(Level.SEVERE, null, ex);
        }
//        }
    }

}
