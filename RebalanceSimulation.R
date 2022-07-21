#
# Read the required libraries.
#
library(ggplot2)
library(dplyr)
library(gt)
library(webshot)
#
# Read the raw csv file if it does not already exist. Exclude rows where the
# ending value is too high.
#
if (!(exists('raw') && is.data.frame(get('raw')))) {
  raw <- read.csv("data/rebalance_simulation1.csv", header=TRUE)
}
#
# Place limits on upside limits on the high, and bear market equity
# allocations.  Place a limit on the ending value of the portfolio that is
# about equal its inflation-adjusted start value of $1,000,000.00. Sort the
# remaining rows first by descending draw, second by descending value.
# 
exclude <- subset(raw, (high <= 0.50) & (bear <= 0.58) &
                    (value <= 10000000.00))
sorted <- exclude[order(-exclude$draw, -exclude$value),]
#
# Get the first one hundred rows, and add a sequence column. Put the sequence
# column first.
#
firstRows <- head(sorted, 100)
firstRows$row <- seq.int(nrow(firstRows))
firstRows <-firstRows[, c("row",
                          "discretionary",
                          "fixed",
                          "high",
                          "bear",
                          "advance",
                          "decline",
                          "value",
                          "draw")]
#
# Create a table. Display it to the viewer, and write it as a
# PDF file.
#
table <-firstRows %>%
  gt() %>%
  tab_options(
    table.font.names = "Ubuntu",
    table.font.size = 12
  ) %>%
  cols_align(
    align = "left",
    columns = everything()
  ) %>%
  fmt_currency(
    columns = c(draw, value)
  ) %>%
  fmt_percent(
    columns = c(discretionary, fixed, high, bear, advance, decline)
  )
table
# gtsave(table, "rebalance_simulation.pdf", zoom = 1)
