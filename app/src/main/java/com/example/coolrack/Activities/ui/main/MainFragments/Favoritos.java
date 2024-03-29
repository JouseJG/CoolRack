package com.example.coolrack.Activities.ui.main.MainFragments;

import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.coolrack.R;
import com.example.coolrack.generalClass.pojos.Libro;

import java.util.ArrayList;

public class Favoritos extends FatherMainFragment{

    public Favoritos() {}

    @Override
    protected void personalizeFragment() {
        this.seccion = "Favoritos";
        getActivity().setTitle(R.string.mainMenu_favoritos);
        this.textoCallback = R.string.item_book_drop_favoritos;
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void cargarLista(){
        this.listBook = (ArrayList<Libro>) this.queryRecord.getFavorito();
    }

    @Override
    protected void personalizeCallback(Libro libro) {
        libro.setFavorito(!libro.getFavorito());
        queryRecord.updateBook(libro);
    }
}