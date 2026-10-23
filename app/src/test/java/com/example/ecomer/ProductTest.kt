package com.example.ecomer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductTest {

    @Test
    fun toFirestoreMap_onlyIncludesFourAllowedFieldsAndExcludesId() {
        val product = Product(
            tenSanPham = "Ap phong nam",
            loaiSanPham = "Thoi trang nu",
            gia = 300000L,
            image = "base64_encoded_data",
            id = "doc_12345"
        )

        val map = product.toFirestoreMap()

        assertEquals(4, map.size)
        assertEquals("Ap phong nam", map["tenSanPham"])
        assertEquals("Thoi trang nu", map["loaiSanPham"])
        assertEquals(300000L, map["gia"])
        assertEquals("base64_encoded_data", map["image"])
        assertFalse("id must NOT be saved to Firestore document", map.containsKey("id"))
        assertTrue(map.keys.containsAll(listOf("tenSanPham", "loaiSanPham", "gia", "image")))
    }
}
