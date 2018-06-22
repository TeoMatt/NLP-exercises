package ex4

import ex3.tt4j.TokenHandler
import ex3.tt4j.TreeTaggerWrapper
import org.omg.CORBA.Object
import java.io.File
import java.io.PrintWriter
import kotlin.math.ceil
import kotlin.math.sqrt

fun main(args: Array<String>) {

    var nasari = mutableMapOf<String,Pair<String, MutableMap<String, Double>>>()

    initializeNasari(nasari)

    var stops = File("./files/utils/stop_words_FULL.txt").readLines().toSet()
    var stops2 = File("./files/utils/function_words.txt").readLines().toSet()
    var stops3 = File("./files/utils/stop_words_1.txt").readLines().toSet()
    var stops4 = File("./files/utils/stop_words__ frakes_baeza-yates.txt").readLines().toSet()
    System.setProperty("treetagger.home", "./tagger")

    var stopSet = stops4.plus(stops).plus(stops2).plus(stops3)

    var doc1Bad = File("./files/ex4/texts_documents/Donald-Trump-vs-Barack-Obama-on-Nuclear-Weapons-in-East-Asia.txt").readLines()
    var doc2Bad = File("./files/ex4/texts_documents/People-Arent-Upgrading-Smartphones-as-Quickly-and-That-Is-Bad-for-Apple.txt").readLines()
    var doc3Bad = File("./files/ex4/texts_documents/The-Last-Man-on-the-Moon--Eugene-Cernan-gives-a-compelling-account.txt").readLines()

    var doc1= mutableListOf<String>()
    var doc2= mutableListOf<String>()
    var doc3= mutableListOf<String>()
    doc1Bad.forEach {
        if(!it.startsWith("#") && !it.startsWith("\n") && !it.equals("")) doc1.add(it)
    }
    doc2Bad.forEach {
        if(!it.startsWith("#") && !it.startsWith("\n") && !it.equals("")) doc2.add(it)
    }
    doc3Bad.forEach {
        if(!it.startsWith("#") && !it.startsWith("\n") && !it.equals("")) doc3.add(it)
    }

    var p = processDocument(doc3, nasari, stopSet).toMutableList()
    p.forEach {
        println(it)
    }

    var daTogliere = ceil((p.size-1).toDouble()*30.0/100.0).toInt()
    println(p.size)
    println(daTogliere)
    var min = 1.0
    var target = 0
    for (i in 1..daTogliere){
        for (j in 1..p.size-1){
            var m = (p[j].third["tit"]!!+p[j].third["int"]!!+p[j].third["ext"]!!)/3.0
            if (m < min){
                min = m
                target = j
            }
        }
        p.removeAt(target)
        println(p.size)
        println("ho rimosso il $target")
        min = 1.0
    }

    var file = File("./files/ex4/results.txt")
    var writer = PrintWriter(file)

    for (j in p){
        println(j.first)
        writer.println(j.first)
    }
    writer.close()

}

fun initializeNasari(nasari: MutableMap<String,Pair<String, MutableMap<String, Double>>>){

    File("./files/ex4/dd-small-nasari-15.txt").readLines().forEach {
    //File("./files/ex4/dd-nasari.txt").readLines().forEach {
        var splittato = it.split(";")
        var m = mutableMapOf<String, Double>()
        for( i in 2..splittato.size-1){
            if(splittato[i].split("_").size>1)
                m.put(splittato[i].split("_")[0], (i-1).toDouble())
        }

        var p = Pair<String, MutableMap<String, Double>>(splittato[0], m)

        nasari.put(splittato[1].toLowerCase(), p)
    }

}

fun processDocument(doc: List<String>, nasari: MutableMap<String,Pair<String, MutableMap<String, Double>>>, stopSet: Set<String>): List<Triple<String, Set<String>, Map<String, Double>>> {

    var processedD = mutableListOf<Triple<String, Set<String>, MutableMap<String, Double>>>()

    var titSet = getLemmatizedSet(doc[0].toLowerCase()).minus(stopSet).minus("").toMutableSet()
    titSet.forEach {
        if (nasari[it] == null){
            titSet = titSet.minus(it).toMutableSet()
        }
    }

    processedD.add(Triple(doc[0], titSet, mutableMapOf("int" to 0.0, "ext" to 0.0, "tit" to 0.0)))

    //calcolo correlazione all'interno di uno stesso paragrafo
    for (i in 1..doc.size-1){

        var intAv = 0.0
        var toRemove = mutableSetOf<String>()
        var s = getLemmatizedSet(doc[i].toLowerCase()).minus(stopSet).minus("").toMutableSet()
        s.forEach {
            if (nasari[it] == null){
                toRemove = toRemove.plus(it).toMutableSet()
            }
        }
        processedD.add(Triple(doc[i], s.minus(toRemove).toMutableSet(), mutableMapOf("int" to 0.0, "ext" to 0.0, "tit" to 0.0)))

        for (it in processedD[i].second) {
            for (it2 in processedD[i].second){
                intAv += weightedOverlap(nasari[it]!!.second, nasari[it2]!!.second)
            }
        }
        processedD[i].third["int"] = intAv/(processedD[i].second.size*2.0)

    }

    //calcolo correlazione tra paragrafi
    for (i in 1..processedD.size-1){

        var extAv = 0.0

        for (j in 1..processedD.size-1){

            var extintAv = 0.0

            for (it in processedD[i].second) {
                for (it2 in processedD[j].second){
                    if (j == i) continue
                    extintAv += weightedOverlap(nasari[it]!!.second, nasari[it2]!!.second)
                }
            }

            extintAv = extintAv/(processedD[i].second.size+processedD[j].second.size)
            //println("media $i con $j : $extintAv, ")
            extAv += extintAv

        }

        processedD[i].third["ext"] = extAv/(processedD.size-2)

    }

    //calcolo correlazione con il titolo
    for (i in 1..processedD.size-1){

        var titAv = 0.0

        for (it in processedD[i].second) {
            for (it2 in processedD[0].second){
                titAv += weightedOverlap(nasari[it]!!.second, nasari[it2]!!.second)
            }
        }

        processedD[i].third["tit"] = titAv/(processedD[i].second.size+processedD[0].second.size)

    }


    return processedD

}

fun weightedOverlap(v1: MutableMap<String, Double>, v2: MutableMap<String, Double>): Double{

    var sum = 0.0
    var c = 0
    for (j in v1){
        if (v2[j.key] != null){
            sum += (1.0 / (j.value + v2[j.key]!!))
            c++
        }
    }

    return when (c) {
        0 -> 0.0
        else -> {
            var somma2 = 0.0
            for (index in 1..c){
                somma2 += (1.0 / (2.0 * index.toDouble()))
            }

            //println(sum/somma2)

            sum/somma2
        }
    }
}

fun getLemmatizedSet(it: String): MutableSet<String>{
    var fraseLemmatizzata = mutableSetOf<String>()
    val tt = TreeTaggerWrapper<String>()
    try {
        tt.setModel("./tagger/lib/english-utf8.par")
        tt.setHandler(object : TokenHandler<String> {
            override fun token(token: String, pos: String, lemma: String) {
                fraseLemmatizzata = fraseLemmatizzata.plus(lemma).toMutableSet()
            }
        })
        tt.process(it.replace("."," . ").replace("“", " \" ").replace("”", " \" ").replace(";", " ; ").replace(":", " : ").replace("'", "' ").split(" ",",").toList())
    } finally {
        tt.destroy()
        return fraseLemmatizzata
    }
}