package com.nytimes.android.external.fs3

import com.nytimes.android.external.fs3.filesystem.FileSystem
import com.nytimes.android.external.store3.base.RecordState
import com.nytimes.android.external.store3.base.impl.BarCode

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit

import okio.BufferedSource

import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.`when`

class RecordPersisterTest {

    @Mock
    internal var fileSystem: FileSystem? = null
    @Mock
    internal var bufferedSource: BufferedSource? = null

    private var sourcePersister: RecordPersister? = null
    private val simple = BarCode("type", "key")

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        sourcePersister = RecordPersister(fileSystem!!, 1L, TimeUnit.DAYS)
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun readExists() {
        `when`(fileSystem!!.exists(simple.toString()))
                .thenReturn(true)
        `when`(fileSystem!!.read(simple.toString())).thenReturn(bufferedSource)

        val returnedValue = sourcePersister!!.read(simple).blockingGet()
        assertThat(returnedValue).isEqualTo(bufferedSource)
    }

    @Test
    fun freshTest() {
        `when`(fileSystem!!.getRecordState(TimeUnit.DAYS, 1L, SourcePersister.pathForBarcode(simple)))
                .thenReturn(RecordState.FRESH)

        assertThat(sourcePersister!!.getRecordState(simple)).isEqualTo(RecordState.FRESH)
    }

    @Test
    fun staleTest() {
        `when`(fileSystem!!.getRecordState(TimeUnit.DAYS, 1L, SourcePersister.pathForBarcode(simple)))
                .thenReturn(RecordState.STALE)

        assertThat(sourcePersister!!.getRecordState(simple)).isEqualTo(RecordState.STALE)
    }

    @Test
    fun missingTest() {
        `when`(fileSystem!!.getRecordState(TimeUnit.DAYS, 1L, SourcePersister.pathForBarcode(simple)))
                .thenReturn(RecordState.MISSING)

        assertThat(sourcePersister!!.getRecordState(simple)).isEqualTo(RecordState.MISSING)
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun readDoesNotExist() {
        `when`(fileSystem!!.exists(SourcePersister.pathForBarcode(simple)))
                .thenReturn(false)

        sourcePersister!!.read(simple).test().assertError(FileNotFoundException::class.java)
    }

    @Test
    @Throws(IOException::class)
    fun write() {
        assertThat(sourcePersister!!.write(simple, bufferedSource!!).blockingGet()).isTrue()
    }

    @Test
    fun pathForBarcode() {
        assertThat(SourcePersister.pathForBarcode(simple)).isEqualTo("typekey")
    }
}
