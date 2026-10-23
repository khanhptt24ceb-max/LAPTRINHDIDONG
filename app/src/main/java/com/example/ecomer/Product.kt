package com.example.ecomer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

/**
 * Model sản phẩm — Firestore document fields:
 *   1. tenSanPham (String)
 *   2. loaiSanPham (String)
 *   3. gia (Long)
 *   4. image (String - Base64, Phần B)
 *
 * Trường [id] được đánh dấu @Exclude để tuyệt đối không lưu lên Firestore.
 */
data class Product(
    var tenSanPham: String = "",
    var loaiSanPham: String = "",
    var gia: Long = 0,
    var image: String = "",

    @get:Exclude
    @set:Exclude
    var id: String = ""
) {
    /**
     * Chuyển sang Map chuẩn đúng 4 trường theo yêu cầu đề bài
     * ("Sinh viên không tự ý thêm các trường khác")
     */
    @Exclude
    fun toFirestoreMap(): Map<String, Any> = hashMapOf(
        "tenSanPham" to tenSanPham,
        "loaiSanPham" to loaiSanPham,
        "gia" to gia,
        "image" to image
    )

    companion object {
        /**
         * Đọc an toàn từ DocumentSnapshot (hỗ trợ cả trường hợp gia lưu kiểu Number hoặc String)
         */
        fun fromDocument(doc: DocumentSnapshot): Product {
            val ten = doc.getString("tenSanPham") ?: ""
            val loai = doc.getString("loaiSanPham") ?: ""
            val giaVal = when (val rawGia = doc.get("gia")) {
                is Number -> rawGia.toLong()
                is String -> rawGia.trim().toLongOrNull() ?: 0L
                else -> 0L
            }
            val img = doc.getString("image") ?: ""
            return Product(
                tenSanPham = ten,
                loaiSanPham = loai,
                gia = giaVal,
                image = img,
                id = doc.id
            )
        }
    }
}
