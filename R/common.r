#load lib
library("ggplot2")
library("plyr")
library("tools")

#clean workspace
rm(list=ls(all=TRUE)) 
#large display
options(width=300)
options(device="svg")

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

