package ex2

import rita.RiWordNet
import java.io.File
import kotlin.math.*

import edu.stanford.nlp.pipeline.*
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.*
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation
import java.io.PrintWriter
import java.util.LinkedList

class Disambiguator(private var dict: RiWordNet){

    var contesti = listOf<String>()
    var parole = listOf<String>()
    var stopSet = setOf<String>()

    fun wsdWithStemming(){

        when {
            parole.isEmpty() -> {
                println("settare una list di parole da disambiguare")
                return
            }
            contesti.isEmpty() -> {
                println("settare una lista di contesti per le parole")
                return
            }
            parole.size != contesti.size -> {
                println("La lista di parole e la lista di contesti hanno lunghezze diverse")
                return
            }
            else -> this.wsdConStemming()
        }

    }

    fun wsdWithLemmatization(){
        when {
            parole.isEmpty() -> {
                println("settare una list di parole da disambiguare")
                return
            }
            contesti.isEmpty() -> {
                println("settare una lista di contesti per le parole")
                return
            }
            parole.size != contesti.size -> {
                println("La lista di parole e la lista di contesti hanno lunghezze diverse")
                return
            }
            else -> {
                var pipeline: StanfordCoreNLP
                try {
                    val props = Properties()
                    props.setProperty("annotators", "tokenize, ssplit, pos, lemma")
                    pipeline = StanfordCoreNLP(props)
                }catch (e: Exception){
                    println("Errore parser stanford")
                    println(e.message)
                    return
                }
                this.wsdConLemmatizzazione(pipeline)
            }
        }
    }

    private fun wsdConStemming() {
        var results = mutableListOf<MutableMap<Int, Int>>()

        var contexts = contesti.map { it
                .toLowerCase()
                .split(" ", "'", ";", ")", "(", ",")
                .toMutableSet().minus(stopSet).minus("")
        }

        var words = parole.map { dict.getStems(it.toLowerCase(),RiWordNet.NOUN)[0] }

        var normalizedContexts = stemming(contexts)

        normalizedContexts.forEach { println(it) }

        println("___________________________________________________________________________________________________________")

        for (iteratore in words.indices) {
            println("La parola è ${words[iteratore]}")
            results.add(mutableMapOf())
            var idArr = dict.getSenseIds(words[iteratore], RiWordNet.NOUN).forEach {
                var vexamplesArr = dict.getExamples(it).map { it
                        .toLowerCase()
                        .split(" ", "'", ";", ")", "(", ",")
                        .toMutableSet().minus(stopSet).minus("")
                }
                var examplesArr: MutableList<Set<String>> = mutableListOf(setOf())
                vexamplesArr.forEach {
                    examplesArr[0] = examplesArr[0].plus(it)
                }
                examplesArr[0] = examplesArr[0].minus(setOf(" ", "'", ";", ")", "(", ",","."))
                println("l'array di esempi è lungo ${examplesArr.size}")
                var normExampleArr = stemming(examplesArr)

                normExampleArr.forEach { println("esempio normalizzato $it") }
                var gloss = dict.getGloss(it).toLowerCase().split(" ", "'", ";", ")", "(", ",").toMutableSet().minus(stopSet).minus("").toMutableSet()

                var normGloss = stemming(gloss)

                if (normExampleArr.isNotEmpty())
                    normExampleArr = normExampleArr.map { it.plus(normGloss).toMutableSet() }.toMutableList()
                else
                    normExampleArr.add(normGloss)

                normExampleArr.forEach { println("QUI$it") }

                println("******************************************************")
                //ADESSO DEVO VEDERE QUALE ESEMPIO HA IL PIÙ ALTO OVERLAP E USO QUELLO PER FARE IL CONFRONTO CON GLI ALTRI SENSI
                var lista = mutableListOf<Int>()
                normExampleArr.forEach { lista.add(abs(it.size - it.minus(normalizedContexts[iteratore]).size)) }
                println(lista)
                println(lista.max())
                results[iteratore].put(it,lista.max()!!)
            }
            println("----------------------------------------------------------")
        }

        println(results)

        var file = File("./files/ex2/results-stemming.txt")
        var writer = PrintWriter(file)

        var p = parole
        var c = contesti
        for (i in p.indices) {
            println("La parola scelta è ${p[i]}")
            println("All'interno della frase: ${c[i]}")
            println("Verrà ora individuato il senso o i sensi con maggior overlap.")
            writer.println("La parola scelta è ${p[i]}")
            writer.println("All'interno della frase: ${c[i]}")
            writer.println("Senso con maggior overlap:")
            var max = 0
            results[i].forEach {
                if (it.value > max) max = it.value
            }
            results[i].forEach {
                if (it.value == max) {
                    println("Synset: ${dict.getSynset(it.key).toList()}")
                    println("Gloss: ${dict.getGloss(it.key)}")
                    println("-----------------------------------------------------------------------------------------")
                }
            }
            for (it in results[i]) {
                if (it.value == max) {
                    writer.println("Synset: ${dict.getSynset(it.key).toList()}")
                    writer.println("Gloss: ${dict.getGloss(it.key)}")
                    writer.println("-----------------------------------------------------------------------------------------")
                    break
                }
            }
            println("____________________________________________________________________________________________")
            writer.println("____________________________________________________________________________________________")
        }
        writer.close()
    }

    private fun stemming(contexts: List<Set<String>>): MutableList<MutableSet<String>> {
        var normalizedContexts = mutableListOf<MutableSet<String>>()
        contexts.forEach {
            normalizedContexts.add(stemming(it))
        }

        return normalizedContexts
    }

    private fun stemming(contexts: Set<String>): MutableSet<String> {
        var normalizedContexts = mutableSetOf<String>()
        contexts.forEach {

            var st = dict.getStems(it.toLowerCase(),RiWordNet.NOUN)
            when{
                st.isEmpty() -> {
                    var stbis = dict.getStems(it.toLowerCase(),RiWordNet.VERB)
                    when{
                        stbis.isEmpty() -> {}
                        stbis.size >= 1 -> normalizedContexts.add(stbis[0])
                    }
                }
                st.isNotEmpty() -> normalizedContexts.add(st[0])
            }
            //println(normalizedContexts)
        }

        return normalizedContexts
    }

    private fun wsdConLemmatizzazione(pipeline: StanfordCoreNLP) {
        var results = mutableListOf<MutableMap<Int, Int>>()

        var normalizedContexts = lemmatizzazione(pipeline, contesti.toMutableList()).map {
            it.minus(stopSet).toMutableSet()
        }

        var words = parole.map { getLemmatization(pipeline, it).toString().split("[","]")[1] }

        println(words)

        normalizedContexts.forEach { println(it) }

        println("___________________________________________________________________________________________________________")

        for (iteratore in words.indices) {
            println("La parola è ${words[iteratore]}")
            results.add(mutableMapOf())
            var idArr = dict.getSenseIds(words[iteratore], RiWordNet.NOUN).forEach {
                var vnormExampleArr = dict.getExamples(it).map {
                    getLemmatization(pipeline, it).toMutableSet().minus(stopSet).toMutableSet()
                }.toMutableList()
                var normExampleArr: MutableList<Set<String>> = mutableListOf(setOf())
                vnormExampleArr.forEach {
                    normExampleArr[0] = normExampleArr[0].plus(it)
                }
                normExampleArr[0] = normExampleArr[0].minus(setOf(" ", "'", ";", ")", "(", ",","."))

                normExampleArr.forEach { println("esempi normalizzati $it") }

                var normGloss = getLemmatization(pipeline, dict.getGloss(it)).toMutableSet().minus(stopSet).toMutableSet()

                if (normExampleArr.isNotEmpty())
                    normExampleArr = normExampleArr.map { it.plus(normGloss).toMutableSet() }.toMutableList()
                else
                    normExampleArr.add(normGloss)

                normExampleArr.forEach { println("Gloss + Esempi : $it") }

                println("******************************************************")
                //ADESSO DEVO VEDERE QUALE ESEMPIO HA IL PIÙ ALTO OVERLAP E USO QUELLO PER FARE IL CONFRONTO CON GLI ALTRI SENSI
                var lista = mutableListOf<Int>()
                normExampleArr.forEach { lista.add(abs(it.size - it.minus(normalizedContexts[iteratore]).size)) }
                println(lista)
                println(lista.max())
                results[iteratore].put(it,lista.max()!!)
            }
            println("----------------------------------------------------------")
        }

        println(results)


        var file = File("./files/ex2/results-Stanford-Lemma.txt")
        var writer = PrintWriter(file)

        var p = parole
        var c = contesti
        for (i in p.indices) {
            println("La parola scelta è ${p[i]}")
            println("All'interno della frase: ${c[i]}")
            println("Verrà ora individuato il senso o i sensi con maggior overlap.")
            writer.println("La parola scelta è ${p[i]}")
            writer.println("All'interno della frase: ${c[i]}")
            writer.println("Senso con maggior overlap:")
            var max = 0
            results[i].forEach {
                if (it.value > max) max = it.value
            }
            results[i].forEach {
                if (it.value == max) {
                    println("Synset: ${dict.getSynset(it.key).toList()}")
                    println("Gloss: ${dict.getGloss(it.key)}")
                    println("-----------------------------------------------------------------------------------------")
                }
            }
            for (it in results[i]) {
                if (it.value == max) {
                    writer.println("Synset: ${dict.getSynset(it.key).toList()}")
                    writer.println("Gloss: ${dict.getGloss(it.key)}")
                    writer.println("-----------------------------------------------------------------------------------------")
                    break
                }
            }
            println("____________________________________________________________________________________________")
            writer.println("____________________________________________________________________________________________")
        }
        writer.close()
    }

    private fun lemmatizzazione(pipeline: StanfordCoreNLP, contexts: MutableList<String>): MutableList<MutableSet<String>> {
        var normalizedContexts = mutableListOf<MutableSet<String>>()
        contexts.forEach {
            normalizedContexts.add(getLemmatization(pipeline, it).toMutableSet())
        }
        return normalizedContexts
    }

    private fun getLemmatization(pipeline: StanfordCoreNLP, s: String): LinkedList<String>{
        // read some text in the text variable
        val text = s

        val lemmas = LinkedList<String>()

        // create an empty Annotation just with the given text
        val document = Annotation(text)

        // run all Annotators on this text
        pipeline.annotate(document)

        // Iterate over all of the sentences found
        val sentences = document.get(SentencesAnnotation::class.java)
        for (sentence in sentences) {
            // Iterate over all tokens in a sentence
            for (token in sentence.get(TokensAnnotation::class.java)) {
                // Retrieve and add the lemma for each word into the list of lemmas
                lemmas.add(token.get(LemmaAnnotation::class.java))
            }
        }

        return lemmas
    }
}




fun main(args: Array<String>) {

    val dis = Disambiguator(RiWordNet("./WordNet-3.0"))

    //var dict = RiWordNet("./WordNet-3.0")
    var stops = File("./files/utils/stop_words_FULL.txt").readLines().toSet()
    var stops2 = File("./files/utils/function_words.txt").readLines().toSet()
    var stops3 = File("./files/utils/stop_words_1.txt").readLines().toSet()
    var stops4 = File("./files/utils/stop_words__ frakes_baeza-yates.txt").readLines().toSet()

    var contesti = File("./files/ex2/contexts.txt")
    var parole = File("./files/ex2/words.txt")

    dis.contesti = contesti.readLines()
    dis.parole = parole.readLines()
    dis.stopSet = stops4.plus(stops).plus(stops2).plus(stops3)
    dis.wsdWithStemming()

    println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")


    dis.contesti = contesti.readLines()
    dis.parole = parole.readLines()
    dis.stopSet = stops4.plus(stops).plus(stops2).plus(stops3)
    dis.wsdWithLemmatization()


}