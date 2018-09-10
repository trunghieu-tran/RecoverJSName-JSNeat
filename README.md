# Recovering Variable Names for Minified Code with Usage Contexts  - JSNeat #

Official website : https://mrstarrynight.github.io/JSNeat/

JSNeat - an information retrieval (IR)- based approach to recover the variable names in minified JS code. JSNEAT follows a data-driven approach to recover names by searching for them in a large corpus of open-source JS code. 

## Why do we create JSNeat? ##
In modern Web development, program understanding plays an equally important role. Web technologies and programming languages require the exposure of source code to Web browsers in the client side to be executed there. To avoid such exposure, the source code such as JavaScript (JS) files are often obfuscated in which the variable names are minified, i.e., the variable names are replaced with short, opaque, and meaningless names. The intention has two folds. First, it makes the JS files smaller and thus are quickly loaded to improve performance. Second, minification diminishes code readability for the readers, while maintaining the program semantics. Due to those reasons, there is a natural need to automatically recover the minified code with meaningful variable names. That's why JSNeat was born.

## Techniques ##
The names of the variables in a particular function not only depend on the task in which the variable is used to implement (called task- specific context), but their names are also affected by their own properties and roles in the code (called single-variable usage context) and on the names of the other variables in the same function (called multiple-variable usage context):
* Single-variable usage context (SVC)
* Multiple-variable usage context (MVC)
* Task-specific context (TSC)

## Empirical Result ##
In this experiment, we evaluate JSNEAT’s accuracy and compare it with the state-of-the-art approaches JSNice and JSNaughty.

Accuracy Comparison

<img src="https://raw.githubusercontent.com/saodem74/RecoverJSName-JSNeat/master/pic/comparison_3tools.png" alt="alt text" width="400" height="300">

Overlapping among Results from Three Tools

<img src="https://raw.githubusercontent.com/saodem74/RecoverJSName-JSNeat/master/pic/overlapping_vein.png" alt="alt text" width="350" height="250">

All experiments were run on a Linux computer server with twenty Intel Xeon 2.2GHz processors, 256GB RAM.

<img src="https://raw.githubusercontent.com/saodem74/RecoverJSName-JSNeat/master/pic/timeComparison.png" alt="alt text" width="300" height="100">


## Corpus ##
We collected a corpus of 12,000 open-source JavaScript projects from GitHub with highest ratings.
Download links: [Corpus](https://raw.githubusercontent.com/mrstarrynight/JSNeat/master/JS-stars-5-ranked-by-stars.csv)


## Contributors ##
* Hieu Tran (trantrunghieu7492@gmail.com)
* Ngoc Mike Tran (ngocmike238@gmail.com)
