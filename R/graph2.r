
source('common.r')

# 1 = input file path for data (wait a csv)
# 2 = output file path for png
args <- commandArgs(TRUE)
path = file_path_as_absolute(args[1])
base_output = file_path_as_absolute(args[2])

files = list.files(path=path, pattern= 'slocal_.*\\.csv')

writeRK <- function (path, file) {
  
  svg_path <- String(basename(file_path_sans_ext(file))) + ".svg"
  csv_path <- String(basename(file_path_sans_ext(file))) + ".csv"
  
  # Load data
  # need csv with "v_idn, v_ticks, v_seed, v_pop column" header to function
  rk <- read.csv(String(path) + "//" + file)
  
  # Compute graphics
  #rk_ordered <- ddply(rk, .(v_seed) , .fun= function(x) { rk[order(-rk$v_pop),] })
  rk_ordered <- ddply(rk, .(v_ticks), .fun= function(x) { x[order(-x$v_pop),] })
  rk_ranked = ddply(rk_ordered, .(v_ticks), transform, v_rank = sort(rank(v_pop, ties.method=c("first"))) )
  
  rk <- ggplot(data=rk_ranked, aes(x=v_rank, y=v_pop, group=v_ticks, colour=v_ticks))
  rk<- rk + scale_colour_gradient()
  rk<- rk + coord_trans(x = "log10", y = "log10")
  rk<- rk + geom_line()
  rk <- rk + scale_y_continuous() + scale_x_continuous() + theme_bw()
  rk <- rk + xlab("Rank of cities") + ylab("Population of cities")
 
  print("Saving " + svg_path + " in " + base_output + "\n") 
  ggsave(rk, file = svg_path, path=base_output ,dpi=600)
  
  print("Saving " + csv_path + " in " + base_output + "\n") 
  write.table(rk_ranked , csv_path, sep="," ,col.names=NA) 
}

for(f in files){
  writeRK(path,f)
}
