# General Lempel–Ziv–Welch (LZW) Compression with 12-Bit Word Size

**Author:** Stephen Tse \<[redacted]@cmu.edu\>


This is an implementation of the [Lempel–Ziv–Welch (LZW)](https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Welch) lossless data compression algorithm, capable of processing any file types (text files, binaries, etc). As you may probably know already, LZW is the first adaptive compression algorithm that introduced the idea of progressive model learning to data compression. As a result, LZW eliminates the need to include a codeword model for each transmission of compressed data, and it normally yields better compression rate than algorithms that must generate a complete model beforehand, such as [Huffman coding](https://en.wikipedia.org/wiki/Huffman_coding), due to more accurate modeling. LZW is widely used in Unix compress, GIF, TIFF, V.42bis modem, etc.

Note that the algorithm performs best on large amount of data that haven't been compressed before, or contain a lot of repetitive data patterns. If you try to use it on compressed data types (e.g. mp4 files), you'll probably notice that the size of the compressed file is large than that of the original one. After all, [Shannon's theorem](https://en.wikipedia.org/wiki/Shannon%27s_source_coding_theorem) is unbeatable even for the greatest compression algorithm! You may be able to further compress your file a bit by using more sophisticated LZW variants such as `Deflate/zlib` (`LZ77` variant + Huffman), but nothing more: there's a theoretical limit on how much a given information can be compressed.


## How to Use

```bash
// compile
javac LZWCompression.java

// compress
java LZWCompression c [original_file_name] [compressed_file_name]

// de-compress
java LZWCompression d [compressed_file_name] [decompressed_file_name]
```


## Implementation Notes

1. All the data structures required by this program are self-implemented except arrays. Note that this implementation uses linked list for queues, hash tables, etc. as per project requirement, but this is not the ideal data structure since the implementation involves a large number of add / pop operations. Change it to array based lists if you want a performance boost, and trade memory usage for some (slightly) more performance gains if you load and encode the entire file into memory first before compression / de-compression.

2. This implementation compresses data into 12-bit words (or data chunks). The codeword model will simply be cleared and start anew when the 12-bit word size is used up. As a result, it doesn't behave well on small amount of data especially with high variance (you may observe a larger file size after compression). This will generally be improved on larger data set (with smaller variance). To improve compression rate on datasets with many distinctive patterns, consider modifying the code to increase the word size; to improve compression rate on general cases (don't know the data pattern beforehand), consider measuring compression effectiveness on the fly and use it to decide when to reset the codeword model instead.
