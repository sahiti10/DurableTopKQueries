# Durable Top-k Queries on Temporal Data

This project implements algorithms from the VLDB 2018 paper:  
**"Durable Top-k Queries on Temporal Data"**  by Junyang Gao, Pankaj K. Agarwal, and Jun Yang.

**Contributors**: Sahiti Adepu, Rakesh Duggempudi, Sharan Kumar Reddy Kodudula

## Overview

Durable top-k queries identify objects that frequently appear in the top-k rankings over a time interval. This project includes implementations of all 7 algorithms from the paper, supporting both fixed-k and variable-k query modes.

## Algorithms Implemented

| Type        | Algorithm           | Accuracy     | Speed              | Variable k |
|-------------|---------------------|--------------|--------------------|------------|
| Exact       | PrefixSum           | Yes          | Moderate           | No         |
| Exact       | IntervalIndex       | Yes          | Fast               | No         |
| Exact       | GeometricPruning    | Yes          | Fast with pruning  | No         |
| Approximate | Sampling            | Approximate  | Very Fast          | Yes        |
| Approximate | ObliviousIndex      | Approximate  | Fast (pre-indexed) | Yes        |
| Approximate | ColumnIndex         | Approximate  | Fast (column-wise) | Yes        |
| Approximate | CellWiseIndex (CEL) | Approximate  | Best overall       | Yes        |

## Project Structure

```
durable-topk/
- /src/durabletopk/              # Java source files for all 7 algorithms
-- TemporalObject.java
-- LoadCSVData.java
-- PrefixSumDurableTopK.java
-- IntervalIndexDurableTopK.java
-- GeometricDurableTopK.java
-- SamplingDurableTopK.java
-- ObliviousIndexDurableTopK.java
-- ColumnIndexDurableTopK.java
-- CellWiseIndexDurableTopK.java    # Core Algorithm
-- FixedKRun.java                   # Main method for execution
-- VariableKRun.java
-- VisualizeResults.java

- /data/
-- Florida_Temp_Data_Preprocessed.csv
-- ar1_dataset.csv
-- dense_stock_synthetic.csv
```
Note: Download the AR(1) dataset here: https://drive.google.com/file/d/18B34krUqIDZLtbeGFZ1gAkcOtq95Swpq/view?usp=drive_link

## How to Run

**Prerequisites**: Java 8 or higher

### Compile the project
```bash
javac -d bin src/durabletopk/*.java
```

### Run Fixed-k Mode
```bash
java -cp bin durabletopk.FixedKRun
```

### Run Variable-k Mode
```bash
java -cp bin durabletopk.VariableKRun
```

Each mode runs experiments on:
- `dense_stock_synthetic.csv` (demo dataset)
- `ar1_dataset.csv` and `Florida_Temp_Data_Preprocessed.csv` (evaluation datasets)

## Parameters

- `k`: Number of top-ranked items to consider
- `tau`: Durability threshold (e.g., 0.05 means 5% of the time interval)
- `startTime`, `endTime`: Time interval for computing durable top-k

## Sample Output

After running the program, you might see output like:

```
---------------------------------------
Config: start=1, end=1000, k=10, tau=0.05
------------------------------------------------
PrefixSum completed in 70 ms
PrefixSum Top-k Result: [75, 76, 77, 78, 79, 80, 81, 82, 83, 84]
----------------------------------
IntervalIndex completed in 72 ms
IntervalIndex Top-k Result: [75, 76, 77, 78, 79, 80, 81, 82, 83, 84]
----------------------------------
Geometric completed in 58 ms
Geometric Top-k Result: [75, 76, 77, 78, 79, 80, 81, 82, 83, 84]
----------------------------------
Sampling completed in 22 ms
Sampling Top-k Result: [73, 76, 77, 78, 79, 80, 81, 82, 83, 84]
```

Additionally, a `results_summary.csv` file is generated containing:
- Algorithm name
- Top-k result size
- Runtime in milliseconds
- Memory used in megabytes
- Precision, Recall, and F1-score (compared to PrefixSum)

## Visualization

To compare algorithm performance visually:

1. Run `FixedKRun` or `VariableKRun` to generate `results_summary.csv`
2. Execute:
```bash
java -cp bin durabletopk.VisualizeResults
```

This will display a bar chart showing:
- Runtime (blue bars) in milliseconds
- Memory usage (green bars) in megabytes
- Comparison across algorithms

## Summary

- Implements all algorithms from the VLDB 2018 paper
- Supports both fixed and variable-k query modes
- Tracks runtime, memory usage, and accuracy metrics
- Provides visualization for performance comparison
