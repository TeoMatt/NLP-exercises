package ex3

import com.babelscape.nasari.docs.NASARIDocument
import ex3.tt4j.*
import it.uniroma1.lcl.babelnet.BabelNet
import it.uniroma1.lcl.babelnet.data.BabelPointer
import it.uniroma1.lcl.jlt.util.Language
import it.uniroma1.lcl.babelnet.BabelNetQuery
import java.io.File
import java.io.PrintWriter
import kotlin.math.abs

var file = File("./files/ex3/results.txt")
var writer = PrintWriter(file)

fun getLemmatizedSet(it: String): MutableSet<String>{
    var fraseLemmatizzata = mutableSetOf<String>()
    val tt = TreeTaggerWrapper<String>()
    try {
        tt.setModel("./tagger/lib/italian-utf8.par")
        tt.setHandler(object : TokenHandler<String> {
            override fun token(token: String, pos: String, lemma: String) {
                fraseLemmatizzata = fraseLemmatizzata.plus(lemma).toMutableSet()
            }
        })
        tt.process(it.replace("."," . ").replace(";", " ; ").replace(":", " : ").replace("'", "' ").split(" ",",").toList())
    } finally {
        tt.destroy()
        return fraseLemmatizzata
    }
}


fun disambiguate(frase: String, parola:String, stopSet: MutableSet<String>){

    writer.println("La frase è: $frase")
    writer.println("La parola da disambiguare è: $parola")
    var fraseLemmatizzata = mutableSetOf<String>()
    fraseLemmatizzata = getLemmatizedSet(frase)

    fraseLemmatizzata = fraseLemmatizzata.minus(stopSet).toMutableSet()
    println (fraseLemmatizzata)


    var bn = BabelNet.getInstance()

    val query = BabelNetQuery.Builder(parola)
            .from(Language.IT)
            .build()
    val byl = bn.getSynsets(query)
    val sl:MutableList<MutableSet<String>> = mutableListOf()
    println(byl.size)


    for (by in byl){
        //println("Sto cercando le parole in relazione con ${by.getGlosses(Language.IT)}")
        //var synsetList = by.getOutgoingEdges(BabelPointer.HYPERNYM)
        var synsetList = by.getOutgoingEdges(BabelPointer.HYPONYM)
        //synsetList.addAll(by.getOutgoingEdges(BabelPointer.HYPONYM))
        //synsetList.addAll(by.getOutgoingEdges(BabelPointer.ANY_MERONYM))
        //synsetList.addAll(by.getOutgoingEdges(BabelPointer.WIKIDATA_HYPERNYM))
        synsetList.addAll(by.getOutgoingEdges(BabelPointer.WIKIDATA_HYPONYM))
        var glossesSet = mutableSetOf<String>()

        by.getGlosses(Language.IT).forEach {
            glossesSet = glossesSet.plus(getLemmatizedSet(it.toString())).toMutableSet()
        }

        for(edge in synsetList) {
            bn.getSynset(edge.babelSynsetIDTarget).getGlosses(Language.IT).forEach {
                var fraseLemmatizzata = mutableSetOf<String>()
                val tt = TreeTaggerWrapper<String>()
                try {
                    tt.setModel("./tagger/lib/italian-utf8.par")
                    tt.setHandler(object : TokenHandler<String> {
                        override fun token(token: String, pos: String, lemma: String) {
                            fraseLemmatizzata = fraseLemmatizzata.plus(lemma).toMutableSet()
                            glossesSet = glossesSet.plus(fraseLemmatizzata).minus(stopSet).toMutableSet()
                        }
                    })
                    tt.process(it.toString().replace("."," . ").replace(";", " ; ").replace(":", " : ").replace("'", "' ").split(" ",",").toList())
                } finally {
                    tt.destroy()
                }
            }
        }

        sl.add(glossesSet.minus(stopSet).toMutableSet())
    }

    var max = 0
    var maxi = 0

    for ((i, it) in sl.withIndex()){
        var a = abs(fraseLemmatizzata.size - (fraseLemmatizzata.minus(sl[i])).size )
        println(a)
        if(a > max){
            max = a
            maxi = i
        }
    }

    println(byl[maxi].getGlosses(Language.IT))
    println(byl[maxi].getLemmas(Language.IT))
    writer.println("La parola è stata disambiguata come: ${byl[maxi].getGlosses(Language.IT)}")
    writer.println("I sinonimi individuati sono: ${byl[maxi].getLemmas(Language.IT)}")
    writer.println()
    writer.println("-----------------------------------------------------------------")
    writer.println()
    writer.flush()
    println()
    println("-----------------------------------------------------------------")
    println()

}


fun main(args: Array<String>) {
    var stopSet = mutableSetOf<String>()
    var stops = File("./files/utils/stopwordsitalian.txt").readLines().forEach {
        if (!it.isEmpty() && !it.first().equals(" ") && !it.first().equals("|")){
            stopSet.add(it.split(" ")[0])
        }
    }
    stopSet = stopSet.plus(".").plus(",").plus(":").plus(";").toMutableSet()
    println(stopSet)

    // Point TT4J to the TreeTagger installation directory. The executable is expected
    // in the "bin" subdirectory - in this example at "/opt/treetagger/bin/tree-tagger_interface"
    System.setProperty("treetagger.home", "./tagger")

    var parole = File("./files/ex3/parole.txt").readLines()

    for ((index, it) in File("./files/ex3/frasi.txt").readLines().withIndex()) {
        disambiguate(it, parole[index], stopSet)
    }

    writer.close()
}