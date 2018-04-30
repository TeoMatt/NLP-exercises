package ex1

import edu.mit.jwi.*
import edu.mit.jwi.item.*
import java.io.File
import edu.mit.jwi.item.ISynset
import edu.mit.jwi.item.POS
import rita.RiWordNet
import kotlin.math.*


// construct the dictionary object and open it

var dict = Dictionary ( File("./WordNet-3.0/dict") )

fun main(args: Array<String>) {

    println(concSimilarityDebug("dog", "cat"))

}

fun concSimilarityDebug(p1: String, p2:String): Double{

    dict.open()

    var w1 = p1
    var w2 = p2

    println(w1)
    println(w2)

    println("-----------------------------------------------------------------")

    val idxWord1 = dict.getIndexWord(w1, POS.NOUN)
    val idxWord2 = dict.getIndexWord(w2, POS.NOUN)

    var cs = 0.0

    for (a in idxWord1.wordIDs){
        for (b in idxWord2.wordIDs){
            var s1 = getSy(a)
            var s2 = getSy(b)

            if (s1 == null || s2 == null)
                return 0.0

            println(s1.gloss)
            println(s1)

            println("Stiamo analizzando il senso in cui il primo sysnset è $s1 e il secondo è $s2")
            var newcs = concSimDebug(s1, s2)
            println(newcs)
            if (newcs >= cs){
                cs = newcs
            }
        }
    }

    println("la conc similarity è $cs")

    println("----------------------------------------------------------------------------------------------------")

    //println(allSeqToEntity(getSy(dict.getIndexWord("entity", POS.NOUN).wordIDs[0])!!))


    if (dict.isOpen)
        dict.close()
    return cs
}

fun concSimilarity(p1: String, p2:String): Double{

    dict.open()

    var w1 = p1
    var w2 = p2

    val idxWord1 = dict.getIndexWord(w1, POS.NOUN)
    val idxWord2 = dict.getIndexWord(w2, POS.NOUN)

    var cs = 0.0

    for (a in idxWord1.wordIDs){
        for (b in idxWord2.wordIDs){
            var s1 = getSy(a)
            var s2 = getSy(b)

            if (s1 == null || s2 == null)
                return 0.0

            var newcs = concSim(s1, s2)
            if (newcs >= cs){
                cs = newcs
            }
        }
    }

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

fun concSimDebug(s1: ISynset, s2: ISynset): Double {

    var l1 = allSeqToEntity(s1)
    var l2 = allSeqToEntity(s2)

    println(l1[0].size)
    println(l2[0].size)

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
                            println(newhlcs)
                            hlcs = newhlcs
                        }else if (somma == (h1+h2)){
                            if (abs((l1[i].size.toDouble())-(l2[j].size.toDouble())) < abs(h1-h2)){
                                h1 = l1[i].size.toDouble()
                                h2 = l2[j].size.toDouble()
                                //ex1.allSeqToEntity(l1[i][c]).forEach { if(it.size-1 > newhlcs){ newhlcs = (it.size-1).toDouble()} }
                                println(newhlcs)
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

    var s = (-log((((h1+h2)-(2*hlcs))/(40)),2.0))/(log(41.0,2.0))

    println(E)

    var numeratore = E.pow(0.6*hlcs)-E.pow(-0.6*hlcs)
    var denominatore = E.pow(0.6*hlcs)+E.pow(-0.6*hlcs)

    s = E.pow(-0.2*(h1+h2-(2*hlcs)))*(numeratore/denominatore)
    println("la similarity diversa è $s")

    hlcs *= 2
    if (hlcs <= (h1 + h2)){
        println(trov)
        return (hlcs/(h1+h2))
    }

    return 0.0
    

}

fun concSim(s1: ISynset, s2: ISynset): Double {

    var l1 = allSeqToEntity(s1)
    var l2 = allSeqToEntity(s2)

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
                        var newhlcs = ((l1[i].size - l1[i].indexOf(l1[i][c]))-1).toDouble()
                        //println(l1[i][c])
                        //PROVO a cambiare calcolando la somma e scegliendo quello con i rami più corti
                        var somma = l1[i].size-1.toDouble() + l2[j].size-1.toDouble()

                        if (somma < (h1+h2)){
                            h1 = l1[i].size-1.toDouble()
                            h2 = l2[j].size-1.toDouble()
                            //ex1.allSeqToEntity(l1[i][c]).forEach { if(it.size-1 > newhlcs){ newhlcs = (it.size-1).toDouble()} }
                            //println(l1[i][c])
                            trov = l1[i][c]
                            hlcs = newhlcs
                        }else if (somma == (h1+h2)){
                            if (abs((l1[i].size-1.toDouble())-(l2[j].size-1.toDouble())) < abs(h1-h2)){
                                h1 = l1[i].size-1.toDouble()
                                h2 = l2[j].size-1.toDouble()
                                //ex1.allSeqToEntity(l1[i][c]).forEach { if(it.size-1 > newhlcs){ newhlcs = (it.size-1).toDouble()} }
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

    hlcs *= 2
    if (hlcs <= (h1 + h2)){
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