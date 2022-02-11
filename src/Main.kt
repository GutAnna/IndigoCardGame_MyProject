package indigo

import kotlin.system.exitProcess

class Game() {

    companion object {
        lateinit var lastWinner: Player
        lateinit var lastLooser: Player

        open class AnyCards() {
            val cards = mutableListOf<String>()

            private fun deleteCards(number: Int): MutableList<String> {
                val deletedCards = mutableListOf<String>()

                if (number !in 1..cards.size) {
                    println(
                        if (number > cards.size) "Invalid number of cards."
                        else "The remaining cards are insufficient to meet the request."
                    )
                    return deletedCards
                }

                repeat(number) {
                    deletedCards.add(cards.removeFirst())
                }
                return deletedCards
            }

            fun shuffle() {
                cards.shuffle()
            }

            fun addCards(number: Int) {
                cards.addAll(deck.deleteCards(number))
            }

            open fun printCards() {
                println(cards.joinToString(separator = " "))
            }
        }

        val deck = AnyCards()

        fun reset() {
            deck.cards.clear()
            val suits = listOf('♦', '♥', '♠', '♣')
            val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
            for (i in suits) {
                for (j in ranks) {
                    deck.cards.add("$j$i")
                }
            }
        }
    }

    object Table : AnyCards() {
        fun printInfo() {
            println()
            if (cards.size != 0) println("${cards.size} cards on the table, and the top card is ${cards.last()}")
            else println("No cards on the table")
        }

        fun isWin(): Boolean {
            if (cards.size < 2) return false
            return cards[cards.size - 1].last() == cards[cards.size - 2].last() ||
                    cards[cards.size - 1].substringBefore(cards[cards.size - 1].last()) ==
                    cards[cards.size - 2].substringBefore(cards[cards.size - 2].last())
        }
    }

    open class Player(private val name: String) : AnyCards() {
        private val winedCards = mutableListOf<String>()
        var score = 0
        override fun printCards() {
            print("Cards in hand: ")
            for (i in cards.indices) {
                print("${i + 1})${cards[i]} ")
            }
            print("\n")
        }

        open fun move() {
            printCards()
            var rightAnswer = false
            while (!rightAnswer) {
                println("Choose a card to play (1-${cards.size}):")
                val step = readLine()!!
                if (validNumber(step)) {
                    if (step.toInt() in 1..cards.size) {
                        rightAnswer = true
                        Table.cards.add(cards.removeAt(step.toInt() - 1))
                    }
                } else if (step == "exit") {
                    println("Game Over")
                    exitProcess(0)
                }
            }
        }

        private fun printScore(computer: Player, player: Player) {
            println("Score: Player ${player.score} - Computer ${computer.score}")
            println("Cards: Player ${player.winedCards.size} - Computer ${computer.winedCards.size}")
        }

        fun win(looser: Player, theEnd: Boolean) {
            Table.cards.forEach() {
                if (it.substringBefore(it.last()) in arrayOf("A", "10", "J", "Q", "K")) this.score++
            }
            this.winedCards.addAll(Table.cards)

            if (!theEnd) {
                println("${this.name} wins cards")
                if (this is Computer) {
                    printScore(this, looser)
                } else {
                    printScore(looser, this)
                }
            }

            if (theEnd) {
                if (this.winedCards.size > looser.winedCards.size) {
                    this.score += 3
                } else {
                    looser.score += 3
                }

                if (this is Computer) {
                    printScore(this, looser)
                } else {
                    printScore(looser, this)
                }
            }

            Table.cards.clear()
            lastWinner = this
            lastLooser = looser
        }
    }

    class Computer(name: String) : Player(name) {
        private val cardsCandidates = mutableListOf<String>()

        private fun chooseCard(cards: MutableList<String>, suit: String? = null, rank: String? = null): String {
            if (suit != null && rank != null) {
                var sameSuit = cards.groupBy { it.last() }.filter { it.key.toString() == suit && it.value.size > 1 }
                    .flatMap { it.value }
                return if (sameSuit.isNotEmpty()) {
                    sameSuit.random()
                } else {
                    val sameRank =
                        cards.groupBy { it.substringBefore(it.last()) }.filter { it.key == rank && it.value.size > 1 }
                            .flatMap { it.value }
                    if (sameRank.isNotEmpty()) {
                        sameRank.random()
                    } else
                        cards.random()
                }
            }

            val equallySuit = cards.groupBy { it.last() }.filter { it.value.size > 1 }.flatMap { it.value }
            return if (equallySuit.isNotEmpty()) {
                equallySuit.random()
            } else {
                val equallyRank =
                    cards.groupBy { it.substringBefore(it.last()) }.filter { it.value.size > 1 }.flatMap { it.value }
                if (equallyRank.isNotEmpty()) {
                    equallyRank.random()
                } else {
                    cards.random()
                }
            }
        }

        override fun move() {
            var card: String
            cardsCandidates.clear()
            var topSuit = ""
            var topRank = ""

            if (Table.cards.size != 0) {
                topSuit = Table.cards.last().last().toString()
                topRank = Table.cards.last().substringBefore(topSuit)
                for (one in cards) {
                    if (one.last()
                            .toString() == topSuit || one.substringBefore(one.last()) == topRank
                    ) cardsCandidates.add(one)
                }
            }

            card = when (cardsCandidates.size) {
                1 -> cardsCandidates.first()
                0 -> chooseCard(cards)
                else -> chooseCard(cardsCandidates, topSuit, topRank)
            }
            println(cards.joinToString(separator = " "))
            cards.remove(card)
            Table.cards.add(card)

            println("Computer plays $card")
        }
    }

    fun twist(first: Player, second: Player) {
        if (first.cards.size == 0 && second.cards.size == 0) {
            if (deck.cards.size == 0) {
                finish()
            } else {
                first.addCards(6)
                second.addCards(6)
            }
        }
        // player
        Table.printInfo()
        first.move()
        if (Table.isWin()) first.win(second, false)
        // computer
        Table.printInfo()
        second.move()
        if (Table.isWin()) second.win(first, false)
    }

    private fun finish() {
        Table.printInfo()
        lastWinner.win(lastLooser, true)
        println("Game Over")
        exitProcess(0)
    }

}

fun validNumber(number: String): Boolean {
    if (number == "") return false
    number.forEach {
        if (!it.isDigit()) {
            return false
        }
    }
    return true
}


fun main() {
    println("Indigo Card Game")
    var rightAnswer = false
    var playFirst = true
    while (!rightAnswer) {
        println("Play first?")
        val input = readLine()!!.lowercase()
        if (input == "yes") {
            playFirst = true
            rightAnswer = true
        } else if (input == "no") {
            rightAnswer = true
            playFirst = false
        }
    }
    val player1 = Game.Player("Player")
    val player2 = Game.Computer("Computer")
    val game = Game()
    Game.reset()
    Game.deck.shuffle()
    print("Initial cards on the table:")
    Game.Table.addCards(4)
    Game.Table.printCards()

    while (true) {
        if (playFirst) game.twist(player1, player2) else game.twist(player2, player1)
    }
}