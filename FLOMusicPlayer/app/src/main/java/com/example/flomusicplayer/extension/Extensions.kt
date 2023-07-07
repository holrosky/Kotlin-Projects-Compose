package com.example.flomusicplayer.extension

fun String.toLyricList(): List<String> =
    split("\n").map{
        it.substringAfter("]")
    }

fun String.toTimeList(): List<Int> =
    split("\n").map {
        it.slice(0..10).toMilliSeconds()
    }

fun String.toMilliSeconds(): Int =
    slice(1..2).toInt() * 60 * 1000 +
    slice(4..5).toInt() * 1000 +
    slice(7..9).toInt()

fun Long.toMinute(): String =
    "${String.format("%02d", this / 1000 / 60)} : ${String.format("%02d", this / 1000 % 60)}"