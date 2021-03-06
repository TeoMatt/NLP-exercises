package ex1

import edu.mit.jwi.*
import edu.mit.jwi.item.*
import java.io.File
import edu.mit.jwi.item.ISynset
import edu.mit.jwi.item.POS
import rita.RiWordNet
import java.io.PrintWriter
import kotlin.math.*


// construct the dictionary object and open it

var dict = Dictionary ( File("./WordNet-3.0/dict") )
var file = File("./files/ex1/results.txt")
var file2 = File("./files/ex1/results_clean.txt")
var writer = PrintWriter(file)
var writer2 = PrintWriter(file2)

fun main(args: Array<String>) {

    var file = File("./files/ex1/500_pairs.txt")
    var wordList = file.readLines()
    wordList.forEach {
        var p1 = it.split("\t")[0]
        var p2 = it.split("\t")[1]
        var res = concSimilarity(p1, p2)
        println("La conc similarity maggiore è ${res}\n")
        println("----------------------------------------------------------------------------------------------------")
        println("----------------------------------------------------------------------------------------------------\n")
        writer.println("La conc similarity maggiore è ${res}\n")
        writer.println("----------------------------------------------------------------------------------------------------")
        writer.println("----------------------------------------------------------------------------------------------------\n")

        writer2.println("$p1 $p2 $res")
    }
    writer.close()
    writer2.close()

}

fun concSimilarity(p1: String, p2:String): Double{

    dict.open()

    var w1 = p1
    var w2 = p2

    println(w1)
    println(w2)
    writer.println(w1)
    writer.println(w2)

    val idxWord1 = dict.getIndexWord(w1, POS.NOUN)
    val idxWord2 = dict.getIndexWord(w2, POS.NOUN)

    if (idxWord1 == null || idxWord2 == null) {
        println("Parola non presente")
        writer.println("Parola non presente")
        return 0.0
    }

    var cs = 0.0

    for (a in idxWord1.wordIDs){
        for (b in idxWord2.wordIDs){
            var s1 = getSy(a)
            var s2 = getSy(b)

            if (s1 == null || s2 == null)
                return 0.0

            println("\nStiamo analizzando il senso in cui il primo sysnset è $s1 e il secondo è $s2")
            writer.println("\nStiamo analizzando il senso in cui il primo sysnset è $s1 e il secondo è $s2")

            var newcs = concSim(s1, s2)
            println(newcs)
            writer.println(newcs)
            if (newcs >= cs){
                cs = newcs
            }
        }
    }


    println("----------------------------------------------------------------------------------------------------")
    writer.println("----------------------------------------------------------------------------------------------------")

    if (dict.isOpen)
        dict.close()
    return cs
}

fun getHypernyms(synset: ISynset): MutableList<ISynsetID>? {

// get the hypernyms
    var hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM)

    return if (hypernyms.isEmpty())
        null
    else
        hypernyms

}

fun getHyponyms(synset: ISynset): MutableList<ISynsetID>? {

// get the hyponyms
    var hyponyms = synset.getRelatedSynsets(Pointer.HYPONYM)

    return if (hyponyms.isEmpty())
        null
    else
        hyponyms

}

fun getSy(noun: IWordID): ISynset? {
    if (!dict.isOpen)
        dict.open()

    val word = dict.getWord(noun)

    return word.synset

}

fun concSim(s1: ISynset, s2: ISynset): Double {

    var l1 = allSeqToEntity(s1)
    var l2 = allSeqToEntity(s2)

    //println(l1[0].size)
    //println(l2[0].size)

    var h1 = 100.0
    var h2 = 100.0

    var hlcs = 0.0

    var i= 0
    var j: Int
    var c: Int
    var d: Int
    var trovato = false
    var trov: ISynset? = null
    while (i < l1.size){
        j = 0
        while (j < l2.size){
            c = 0
            while (c < l1[i].size && !trovato){
                d = 0
                while (d < l2[j].size && !trovato){
                    if (l1[i][c] == l2[j][d] && (l1[i].size - l1[i].indexOf(l1[i][c])) == (l2[j].size - l2[j].indexOf(l2[j][d]))){
                        trovato = true
                        var newhlcs = ((l1[i].size - l1[i].indexOf(l1[i][c]))).toDouble()
                        //println(l1[i][c])
                        //PROVO a cambiare calcolando la somma e scegliendo quello con i rami più corti
                        var somma = l1[i].size.toDouble() + l2[j].size.toDouble()

                        if (somma < (h1+h2)){
                            h1 = l1[i].size.toDouble()
                            h2 = l2[j].size.toDouble()
                            //ex1.allSeqToEntity(l1[i][c]).forEach { if(it.size-1 > newhlcs){ newhlcs = (it.size-1).toDouble()} }
                            //println(l1[i][c])
                            trov = l1[i][c]
                            //println(newhlcs)
                            hlcs = newhlcs
                        }else if (somma == (h1+h2)){
                            if (abs((l1[i].size.toDouble())-(l2[j].size.toDouble())) < abs(h1-h2)){
                                h1 = l1[i].size.toDouble()
                                h2 = l2[j].size.toDouble()
                                //ex1.allSeqToEntity(l1[i][c]).forEach { if(it.size-1 > newhlcs){ newhlcs = (it.size-1).toDouble()} }
                                //println(newhlcs)
                                hlcs = newhlcs
                            }
                        }
                    }
                    d++
                }
                c++
            }
            j++
            trovato = false
        }
        i++
    }


    println("l'antenato ha altezza: $hlcs")
    println("un ramo ha altezza $h1, l'altro ha altezza $h2")
    writer.println("l'antenato ha altezza: $hlcs")
    writer.println("un ramo ha altezza $h1, l'altro ha altezza $h2")


    //var s = (-log((((h1+h2)-(2*hlcs))/(40)),2.0))/(log(41.0,2.0))

    //println(E)

    //var numeratore = E.pow(0.6*hlcs)-E.pow(-0.6*hlcs)
    //var denominatore = E.pow(0.6*hlcs)+E.pow(-0.6*hlcs)

    //s = E.pow(-0.2*(h1+h2-(2*hlcs)))*(numeratore/denominatore)
    //println("la similarity diversa è $s")

    hlcs *= 2
    if (hlcs <= (h1 + h2)){
        println(trov)
        writer.println(trov)
        return (hlcs/(h1+h2))
    }

    return 0.0
    

}

fun allSeqToEntity(s: ISynset): MutableList<MutableList<ISynset>>{

    if (!dict.isOpen)
        dict.open()

    var l = mutableListOf(s)
    var listoflists = mutableListOf<MutableList<ISynset>>()

    visitaProf(s, l, listoflists)

    return listoflists
}

fun visitaProf(s: ISynset, m: MutableList<ISynset>, mm :MutableList<MutableList<ISynset>>){
    if (!dict.isOpen)
        dict.open()

    var hypernyms = getHypernyms(s)
    if (hypernyms == null) {
        var nuova = mutableListOf<ISynset>()
        m.forEach { nuova.add(it) }
        mm.add(nuova)
        return
    } else {
        for (i in hypernyms){
            m.add(dict.getSynset(i))
            visitaProf(dict.getSynset(i), m, mm)
            m.remove(dict.getSynset(i))
        }
    }
}