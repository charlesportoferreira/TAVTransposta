mat=read.table("discover.txt",sep=",")
mat2=t(mat)
write.table(mat2,file="Tdiscover.txt",sep=",",col.names=F, row.names=F)