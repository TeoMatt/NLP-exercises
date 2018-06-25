# NLP-exercises
This repository contains all the exercises developed for TLN course (2017/2018) - University of Turin.
For these exercises [Kotlin Programming Language](https://kotlinlang.org/) had been used.
## Ex1
**Concept Similarity in WordNet**.

For this exercise I used WorNet structure and I implemented *Wu Palmer* concept similarity formula. Given the fact that every synset could have multiple hypernyms, I decided to find every possible path to the entity root starting from the two examined synsets. At this point I used these paths to find the lowest common subsumer of the examined sysnsets and used the formula to calculate the similarity. The program prints the similarity of the two most similar senses of the given pairs of words.

The file `results.txt` contains the calculated values for every pair of senses and the the biggest one. The `results_clean.txt` contains only the pair and the biggest calculated similarity, but no indications about the senses.

### Libraries
* [JWI 2.4.0 - MIT Java Wordnet Interface](https://projects.csail.mit.edu/jwi/)

## Ex2
**Word Sense Disambiguation in WordNet**

For this exercise I used WordNet structure visiting it with RiTa library. For the first par of the exercise I used `getStem` method which use WordNet stemmatization. For the second part I used Stanford parser which at first uses the PoS tagger and then lemmatizes the given words.

The results are shown separately in the two files `results-stemming.txt` and `results-Stanford-Lemma.txt`. The only difference between the two files is in the word ash, correctly disambiguated only using the stemming (the reason of this error is probably due to an incorrect tagging and consequently wrong lemmatization of the word burnt, using Stanford PoS tagger).

### Libraries
* [RiTa](https://rednoise.org/rita/)
* [Stanford CoreNLP – Natural language software](https://stanfordnlp.github.io/CoreNLP/)

## Ex3
**Word Sense Disambiguation in BabelNet**

For this exercise I visited BabelNet structure using the Java APIs. Similarly as in the previous exercise, I used the glosses of the word to disambiguate, but this time I also added the glosses from every hyponym of the given word. I tried different combinations of relations to add, for example I added hypernyms' glosses instead of hyponyms' ones, or a mix of the glosses of the two relations. I also tried using meronyms but the best results showed up using only the hyponyms, as you can see in the `results.txt` file.

### Libraries

* [BabelNet 4.0 Java API](https://babelnet.org/guide)
* [TreeTagger - a part-of-speech tagger for many languages](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/)
* [TreeTagger for Java by reckart](https://github.com/reckart/tt4j)

## Ex4
**Automatic Summarization**

For this exercise I used NASARI vectors to operate an automatic summarization on the 3 given texts. To determine which paragraphs were to delete I used 3 values: the first one is a measure of the internal cohesion of the paragraph, the second one is a measure of the cohesion with the other paragraphs and the last one is the cohesion with the title. Every measure has been calculated through a weighted overlap formula, considering the rank of the overlapping dimensions in the NASARI vectors. I based the decision to maintain or delete a paragraph on a mean of these three values. The value `summarizationFactor` can be used to decide how many paragraph are to remove.

I decided to consider a paragraph approach and not a sentence approach because of the reduced NASARI resource I used, in which I couldn't find many of the words I had to analyze. The result was that some paragraphs had only one vector to use in the comparisons.

### Libraries

* [NASARI - A Novel Approach to a Semantically-Aware Representation of Items](http://lcl.uniroma1.it/nasari/)
* [TreeTagger - a part-of-speech tagger for many languages](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/)
* [TreeTagger for Java by reckart](https://github.com/reckart/tt4j)

