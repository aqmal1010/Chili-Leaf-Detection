package com.example.chilidetectionapp.ui.history

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chilidetectionapp.R
import com.example.chilidetectionapp.adapter.HistoryAdapter
import com.example.chilidetectionapp.data.Detection
import com.example.chilidetectionapp.databinding.FragmentHistoryBinding
import com.example.chilidetectionapp.ui.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class HistoryFragment : Fragment(), HistoryAdapter.ItemClickListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            show()
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.white)))
            title = SpannableString(title ?: "").apply {
                setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.green_primary)), 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }

        historyViewModel = obtainViewModel(this)
        historyAdapter = HistoryAdapter(emptyList(), this)
        binding.historyRv.adapter = historyAdapter
        binding.historyRv.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRv.setHasFixedSize(true)

        // Swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedItem = historyAdapter.currentList[position]

                // Hapus dari database
                historyViewModel.delete(deletedItem)

                // Tampilkan snackbar dengan opsi undo
                Snackbar.make(binding.root, "Riwayat dihapus", Snackbar.LENGTH_LONG)
                    .setAction("BATAL") {
                        historyViewModel.insert(deletedItem)
                    }.show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_primary_light))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_primary_light))
                    .addSwipeRightActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })


        itemTouchHelper.attachToRecyclerView(binding.historyRv)

        historyViewModel.allDetection.observe(viewLifecycleOwner) { analyzeList ->
            historyAdapter.updateData(analyzeList)
            binding.tvEmptyState.visibility = if (analyzeList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun obtainViewModel(fragment: Fragment): HistoryViewModel {
        val factory = ViewModelFactory(fragment.requireContext())
        return ViewModelProvider(fragment, factory)[HistoryViewModel::class.java]
    }

    override fun onItemClick(analyze: Detection) {
        val intent = Intent(requireContext(), DetailHistoryActivity::class.java)
        intent.putExtra(
            DetailHistoryActivity.EXTRA_ANALYZE_ID,
            analyze
        )
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}