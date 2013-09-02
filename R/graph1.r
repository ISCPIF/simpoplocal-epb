source('common.r')

# 1 = input file path for data (wait a csv)
# 2 = output file path for png
args <- commandArgs(TRUE)
path = args[1]
base_output = file_path_as_absolute(args[2]) 

# Load data
# need csv with "v_idn, v_ticks, v_seed, v_pop column" header to function
all_rk <- read.csv(path)

# Compute graphics
rk_ordered <- ddply( all_rk, .(v_seed) , .fun= function(x) { x[order(-x$v_pop),] } )
rk_ranked <- ddply( rk_ordered, .(v_seed) , transform, v_rank = sort(rank(v_pop, ties.method=c("first")))) 

rk<- ggplot(rk_ranked, aes(x = v_rank,y=v_pop ))
rk <- rk + geom_boxplot( aes(group = v_rank), size=0.2, fatten = 0.4 , outlier.colour = "red", outlier.size = 1.0) 
rk <- rk + stat_summary(fun.y = mean,aes(shape="mean"), geom="point", color="red", shape=4) 
rk <- rk + coord_trans(x = "log10", y = "log10") 
rk <- rk + theme_bw() + xlab('Rank of cities') + ylab('Population of cities')

print(basename(file_path_sans_ext(path)))

svg_path <- String(basename(file_path_sans_ext(path))) + ".svg"
csv_path <- String(basename(file_path_sans_ext(path))) + ".csv"
  
print("Saving " + svg_path + " in " + base_output + "\n") 
ggsave(rk, file = svg_path, path=base_output ,dpi=600)

print("Saving " + csv_path + " in " + base_output + "\n") 
write.table(rk_ranked , csv_path, sep="," ,col.names=NA) 
