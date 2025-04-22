# Durable Top-k Queries on Temporal Data
This repository contains a complete Java-based implementation of the algorithms presented in the VLDB 2018 paper:  
"Durable Top-k Queries on Temporal Data" by Junyang Gao, Pankaj K. Agarwal, and Jun Yang.

Implemented by: Sahiti Adepu, Rakesh Duggempudi, Sharan Kumar Reddy Kodudula

## Overview

Durable top-k queries identify objects that appear in the top-k results over a sufficiently large fraction of time within a given interval. This project implements all seven core systems and algorithms introduced in the paper, including exact and approximate methods.

## Algorithms Implemented

| Category        | Algorithm              | Accuracy         | Performance          | Supports Arbitrary 'k'   |
|-----------------|------------------------|------------------|----------------------|--------------------------|
| Exact           | PrefixSum              | Exact            | Baseline             | No                       |
| Exact           | IntervalIndex          | Exact            | Optimized Indexing   | No                       |
| Exact           | GeometricPruning       | Exact            | Pruned Ranking       | No                       |
| Approximate     | Sampling               | Approximate      | Fast(random sampling)| Yes                      |
| Approximate     | ObliviousIndex         | Approximate      | Fast (precomputed)   | Yes                      |
| Approximate     | ColumnIndex            | Approximate      | Fast (column-wise)   | Yes                      |
| Approximate     | CellWiseIndex (CEL)    | Approximate      | Fast and Recommended | Yes                      |

## Source Code Files

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
Requirements: Java 8 or later

To Compile:
```bash
javac -d bin src/durabletopk/*.java
```
To Run:
```bash
java -cp bin durabletopk.ExperimentalRunner
```

This will:
- Run all seven algorithms on the `dense_stock_synthetic.csv` file as a demo
- Evaluate performance on two additional datasets (`ar1_dataset.csv`, `Florida_Temp_Data_Preprocessed.csv`)

## Parameters and Terminology

- `k`: Number of top-ranked objects to consider at each timestamp.
- `τ` (tau): Durability threshold (e.g., `τ = 0.6` means an object must appear in top-k for 60% of the time interval).
- `startTime`, `endTime`: Time range over which the query is evaluated.

## Datasets Used

| Dataset Name                        | Description                                                       |
|------------------------------------ |------------------------------------------------------------------ |
| `Florida_Temp_Data_Preprocessed.csv`| Weather sensor data from Florida stations                         |
| `ar1_dataset.csv`                   | Synthetic dataset using AR(1) process                             |
| `dense_stock_synthetic.csv`         | Preprocessed real world stock dataset with consistent top-k ranks |

## Output Format

For each algorithm, the following is displayed:
- The set of object IDs satisfying the durable top-k condition
- Execution time in milliseconds

Example:

```
PrefixSum Top-k Result: [75, 76, ..., 99]
PrefixSum completed in 69 ms
```

## Key Observations

- The `PrefixSum` algorithm serves as the reference baseline.
- All exact algorithms match its result set.
- Approximate algorithms may include noise but offer significant runtime benefits.
- `CellWiseIndex` provides the best trade-off and reflects the core proposal of the research paper.

## Evaluation Notes

- The current implementation of `CellWiseIndex` includes a flat-array optimized version.
- This ensures accurate results and fast execution aligned with the research paper’s performance goals.
- For sparse or non-contiguous object IDs, preprocessing them into dense indices is recommended.

## Visualization Support

The project includes a Java Swing-based bar chart visualization to compare algorithm runtimes and memory usage.

### How to Use:
1. After running `ExperimentalRunner.java`, ensure that `results_summary.csv` is generated.
2. Compile and run the visualization:

```bash
javac -d bin src/durabletopk/VisualizeResults.java
java -cp bin durabletopk.VisualizeResults.java
```

### Output:
- A bar chart window will display:
  - **Blue bars** represent runtime (in milliseconds)
  - **Green bars** represent memory usage (in MB)
- Each algorithm is labeled and compared visually.

This visualization helps validate experimental performance, interpret trade-offs, and enhance the demonstration value of your project.

