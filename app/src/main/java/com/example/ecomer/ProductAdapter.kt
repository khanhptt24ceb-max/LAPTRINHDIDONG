package com.example.ecomer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.RecyclerView
import com.example.ecomer.databinding.ItemProductBinding

/**
 * RecyclerView Adapter cho danh sách sản phẩm.
 * Tích hợp LruCache để cache ảnh Base64 giúp cuộn mượt mà.
 */
class ProductAdapter(
    private val products: MutableList<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var isAdmin = true

    // Bộ nhớ đệm Bitmap tránh decode lại Base64 nhiều lần khi scroll
    private val bitmapCache = object : LruCache<String, Bitmap>(20) {}

    inner class ViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        holder.binding.apply {
            // Hiển thị đúng format đề bài: Tên sp, Giá sp, Loại sp
            tvTenSp.text = buildSpannedString {
                bold { append("Tên sp: ") }
                append(product.tenSanPham)
            }
            tvGiaSp.text = buildSpannedString {
                bold { append("Giá sp: ") }
                append(product.gia.toString())
            }
            tvLoaiSp.text = buildSpannedString {
                bold { append("Loại sp: ") }
                append(product.loaiSanPham)
            }

            // Hiển thị ảnh từ chuỗi Base64 (Phần B)
            if (product.image.isNotEmpty()) {
                val cacheKey = "${product.id}_${product.image.hashCode()}"
                val cached = bitmapCache.get(cacheKey)
                if (cached != null) {
                    imgProduct.setImageBitmap(cached)
                } else {
                    try {
                        val bytes = Base64.decode(product.image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            bitmapCache.put(cacheKey, bitmap)
                            imgProduct.setImageBitmap(bitmap)
                        } else {
                            imgProduct.setImageResource(R.drawable.ic_placeholder)
                        }
                    } catch (e: Exception) {
                        imgProduct.setImageResource(R.drawable.ic_placeholder)
                    }
                }
            } else {
                imgProduct.setImageResource(R.drawable.ic_placeholder)
            }

            // Phân quyền: ẩn/hiện nút Sửa & Xóa (Phần C)
            layoutActions.visibility = if (isAdmin) View.VISIBLE else View.GONE

            btnEdit.setOnClickListener { onEdit(product) }
            btnDelete.setOnClickListener { onDelete(product) }
        }
    }

    override fun getItemCount(): Int = products.size

    fun setAdminMode(admin: Boolean) {
        isAdmin = admin
        notifyDataSetChanged()
    }
}
