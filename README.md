# Similarity Analyser
The aim of this tool is to help manage the problem of program plagiarism by identifying pairs of programs, written in Java, given a set of programs that exhibit an especially high degree of similarity.

# Quick Start
Given a set of programs, it produces 3 scores based around the structure of the code and text-based similarity in the program.

To run an analysis:

1. Run SimAnalyser.jar
2. Select a folder which contains only .java files (due to an issue with the parser, the java files must ONLY contain valid java code and not contain ANY comments after the closing bracket)
3. Select Open - This will run the analysis and create 3 text files in the folder ABOVE the select folder.
4. To view the results you can either open the text file, or run SimViewer.jar and then select the open file

# Things to note
A small number of correct Java programs can crash the parser

This is not entirely my own work, but I will update this soon with clarification about which bits came from where

# Credits

Olav Skjelkvåle Ligaarden - http://folk.uio.no/olavsli/

JavaCC - https://javacc.java.net/

JavaParser: https://github.com/javaparser/javaparser

I’m currently working on documentation which will be up shortly. (27/06/16)

The report I wrote alongside this work is available on request. 

Any issues or questions can be sent to - similarityanalyser at gmail dot com
