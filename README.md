
# Durable Top-k Queries on Temporal Data

This repository provides a full Java implementation of the algorithms from the VLDB 2018 paper:  
**"Durable Top-k Queries on Temporal Data"**  by Junyang Gao, Pankaj K. Agarwal, and Jun Yang.

**Implemented by**: Sahiti Adepu, Rakesh Duggempudi, Sharan Kumar Reddy Kodudula

## Overview

Durable top-k queries identify objects that appear in the top-k rankings for a sufficient fraction (`τ`) of a given time interval. This project implements all seven algorithms from the paper, with support for both fixed-k and variable-k query settings.

## Algorithms Implemented

| Type        | Algorithm              | Accuracy      | Performance           | Arbitrary k Supported |
|-------------|------------------------|----------------|------------------------|------------------------|
| Exact       | PrefixSum              | Exact          | Moderate               | No                     |
| Exact       | IntervalIndex          | Exact          | Efficient              | No                     |
| Exact       | GeometricPruning       | Exact          | Fast with pruning      | No                     |
| Approximate | Sampling               | Approximate    | Very fast              | Yes                    |
| Approximate | ObliviousIndex         | Approximate    | Fast (pre-indexed)     | Yes                    |
| Approximate | ColumnIndex            | Approximate    | Fast (column-wise)     | Yes                    |
| Approximate | CellWiseIndex (CEL)    | Approximate    | Best trade-off         | Yes                    |

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
-- CellWiseIndexDurableTopK.java
-- ExperimentalRunner.java
-- VisualizeResults.java

- /data/
-- Florida_Temp_Data_Preprocessed.csv
-- ar1_dataset.csv
-- dense_stock_synthetic.csv
```
Note: Download the AR(1) dataset here: https://drive.google.com/file/d/18B34krUqIDZLtbeGFZ1gAkcOtq95Swpq/view?usp=drive_link


## Compilation and Execution

**Prerequisites**: Java 8 or later

### Compile
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

## Parameters and Terminology

- `k`: Number of top-ranked objects to consider at each timestamp.
- `τ (tau)`: Durability threshold (e.g., `τ = 0.05` means 5% of the time interval).
- `startTime`, `endTime`: The interval over which durable top-k is computed.

## Datasets Used

| Dataset Name                          | Description                                                       |
|--------------------------------------|-------------------------------------------------------------------|
| `dense_stock_synthetic.csv`          | Synthetic stock values with strong top-k patterns                 |
| `ar1_dataset.csv`                    | AR(1) generated series to test scalability and durability logic   |
| `Florida_Temp_Data_Preprocessed.csv` | Real-world temperature readings from Florida weather stations     |

## Output Format

For each algorithm, the following output is printed:

```
<Algorithm> Top-k Result: [id1, id2, ..., idk]
<Algorithm> completed in <X> ms
```

And logged in `results_summary.csv` with:

- Algorithm name
- Top-k result size
- Runtime in milliseconds
- Memory used in megabytes
- Precision, Recall, and F1 score (compared to PrefixSum)

## Evaluation Observations

- PrefixSum serves as the baseline for correctness.
- All exact algorithms match its results.
- Approximate algorithms may produce varied results but offer better runtime.
- CellWiseIndex performs best in terms of the trade-off between speed and accuracy.

## Visualization Support

A built-in Swing-based visualization tool allows comparison of algorithm runtimes and memory usage.

### To Use

1. Run `FixedKRun` or `VariableKRun` to generate `results_summary.csv`
2. Then execute:

```bash
java -cp bin durabletopk.VisualizeResults
```

### Output

A bar chart showing:

- Runtime (blue bars) in milliseconds
- Memory usage (green bars) in megabytes
- Per algorithm comparison

## Summary

- This project replicates the core ideas and experiments from the VLDB 2018 paper.
- Supports both fixed and variable-k modes.
- Includes runtime and memory tracking, result accuracy (F1), and Java-based visualization.
- Demonstrates scalability and effectiveness across synthetic and real-world datasets.
