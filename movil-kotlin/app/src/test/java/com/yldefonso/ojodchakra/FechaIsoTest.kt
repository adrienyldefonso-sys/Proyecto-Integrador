package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.red.FechaIso
import org.junit.Assert.assertEquals
import org.junit.Test

class FechaIsoTest {

    @Test
    fun elCeroEsElInicioDeLaEraUnixEnUtc() {
        assertEquals("1970-01-01T00:00:00.000Z", FechaIso.desdeMillis(0L))
    }

    @Test
    fun conservaLosMilisegundos() {
        assertEquals("2023-11-14T22:13:20.123Z", FechaIso.desdeMillis(1_700_000_000_123L))
    }
}