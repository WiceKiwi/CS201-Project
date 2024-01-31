# CS201-Project
Data Structures and Algorithms - Application of tree structure on data compression  

Group members: Oliver Loh, Jana Trisha Tanchan Go, Tan Zhi Rong, Nicholas Pey, Justin Dalva Wicent

## Goals
- Exploring tree-based compression techniques with lossy/lossless implementation
- Experiment with possible changes to the chosen implementation (alternative data structures, changes/modifications to the actual algorithm, configurations/parameters, etc.)
- Test the implementation with Java Application

## Chosen Tree-based Implementation
- QuadTree
- JPEG (Huffman Encoding)

## What We Have Experimented
### Storing Color
We have tried to store the color in the datatype int instead of int[]. int[] uses more memory allocation compared to int data type.
### Maximum Depth of QuadTree
The maximum depth specify the maximum level of recursion in the compression process.
### Error Threshold
Error Threshold represents a threshold for error in color similarity during compression process. For easier value adjustment, we use absolute threshold for error calculation and represent it as a percentage.

## Directories
| Folder | Description |
| -- | -- |
| Original | Contains the original images for testing |
| Compressed | Contains .bin files after compression |
| Decompressed | Contains the images after being decompressed |

- App.java contains the main method
- Utility.java contains the method for compression and decompression
