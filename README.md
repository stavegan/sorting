
# Sorting

### Command line tool to sort files of unlimited size.

#### Explanations:
* Sorting is ascending.
* Numbers come before lines. 
* Strings are sorted lexicographically.
* Invalid input files are skipped with a message.
* Invalid lines (containing spaces) are skipped.

#### Usage:
* the first ```n - 1``` arguments are the names of the input files to be sorted,
* the last ```n``` argument presents the directory to save the sorting results.

##### Example:
```sbt "run files.txt to.txt be.txt sorted.txt ~/Downloads"```

#### Output:
* ```{directory}/sorted.txt``` – the name of the sorted file.
If a file with such a name was passed as arguments to input files, it will be read and overwritten.
* ```{directory}/sorted-UUID.txt``` – the name of the temporary file.
The file is deleted after sorting is completed.
It is needed to store intermediate results outside the memory heap.
