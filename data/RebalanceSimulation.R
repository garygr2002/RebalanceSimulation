#
# Read the required libraries.
#
library(dplyr)
library(gt)
library(webshot)
#
# Read the raw csv file. Exclude rows where the ending value is
# too high. Sort first by draw, then by value.
#
raw <- read.csv("rebalance_simulation.csv", header=TRUE)
exclude <- subset(raw, value <= 10000000.00)
sorted <- exclude[order(-exclude$draw, -exclude$value),]
#
# Get the first fifty rows, and add a sequence column. Put
# the sequence column first.
firstRows <- head(sorted, 50)
firstRows$row <- seq.int(nrow(firstTen))
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
gtsave(table, "rebalance_simulation.pdf", zoom = 1)
