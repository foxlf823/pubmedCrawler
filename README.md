# A Tool for Downloading PubMed Abstracts

This tool is used to download PubMed abstracts via [Entrez Programming Utilities](https://www.ncbi.nlm.nih.gov/books/NBK25501/), which is useful for training word embeddings using some language models such as word2vec.

Requirements
-----
* just Java and web connection

Usage
-----
1. Compile "Main.java".

  ```
  javac Main.java
  ```

2. Use the following command.

  ```
  java Main ./output key_word.txt 20 5 1
  ```
  * output - the output directory
  * key_word.txt - the key words for searching in PubMed
  * 20 - the max number of returned abstracts for each query
  * 5 - the number of abstracts that you want to download for each key word
  * 1 - the start line number in key_word.txt

Notes
-----
Usually, it can download several thousand abstracts per day. If you have a great number of key words, you can run several processes simultaneously. If the tool stops or halts, you can kill the process and restart it, such as:

  ```
  java Main ./output key_word.txt 20 5 new_start_line_number
  ```

It will restart to download from "new_start_line_number". If the output directory contains previously downloaded abstracts, the tool won't download them reduplicatively.
