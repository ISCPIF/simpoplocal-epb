#load lib
library("ggplot2")
library("plyr")

#clean workspace
rm(list=ls(all=TRUE)) 
#large display
options(width=300)

# Utils function 
# see http://stackoverflow.com/questions/4730551/making-a-string-concatenation-operator-in-r?lq=1
String <- function(x) {
  class(x) <- c("String", class(x))
  x
}

"+.String" <- function(x,...) {
  x <- paste(x, paste(..., sep="", collapse=""), sep="", collapse="")
  String(x)
}

print.String <- function(x, ...) cat(x)

# 1 = input file path for data (wait a csv)
# 2 = output file path for png
args <- commandArgs(TRUE)
path = args[1]
output <- String(args[2]) + "rk_variability.png"

# Load data
# need csv with "v_idn, v_ticks, v_seed, v_pop column" header to function
all_rk <- read.csv(path)

# Compute graphics
rk_ordered <- ddply( all_rk, .(v_seed) , .fun= function(x) { x[order(-x$v_pop),] } )
rk_ranked <- ddply( rk_ordered, .(v_seed) , transform, v_rank = sort(rank(v_pop, ties.method=c("first")))) 

png(output)
rk<- ggplot(rk_ranked, aes(x = v_rank,y=v_pop ))
rk <- rk + geom_boxplot( aes(group = v_rank), size=0.2, fatten = 0.4 , outlier.colour = "red", outlier.size = 1.0) 
rk <- rk + stat_summary(fun.y = mean,aes(shape="mean"), geom="point", color="red", shape=4) 
rk <- rk + coord_trans(x = "log10", y = "log10") 
rk <- rk + theme_bw() + xlab('Rank of cities') + ylab('Population of cities')
rk
garbage <- dev.off()

