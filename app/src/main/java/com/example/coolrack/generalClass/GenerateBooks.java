package com.example.coolrack.generalClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.example.coolrack.R;
import com.example.coolrack.generalClass.ImagesManagers.BitmapManager;
import com.example.coolrack.generalClass.SQLiteControll.QueryRecord;
import com.example.coolrack.generalClass.pojos.Libro;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;

public class GenerateBooks {
    QueryRecord queryRecord;
    Context context;

    public GenerateBooks(Context context){
        this.queryRecord = QueryRecord.get(context);
        this.context = context;
    }

    public void searchLibors() {

        try {
        //saca la ruta de Descargas y lo usa en el objeto dir
        String path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        File dir = new File(path);

        EpubReader er = new EpubReader();
        for (File f :dir.listFiles()){
            Libro l = null;
            try {
                Book b = er.readEpub(new FileInputStream(f.getAbsolutePath()));
                l = setData(b, f.getAbsolutePath(),false);

                if(l != null)
                    queryRecord.setNewBook(l);
                System.out.println(f.getAbsolutePath());

            } catch (Exception e){
                e.printStackTrace();
            }finally {
                l = null;
            }
        }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // retorna la direccio de la copia del libro a leer
    // añade un nuevo libro la DB asi como generar una copia privada para el programa
    public String addLibroInDB (File file, boolean leyendo){
        Libro l = null;
        Book book = null;

        try {
            InputStream epubInputStream = new BufferedInputStream(new FileInputStream(file));
            book = (new EpubReader()).readEpub(epubInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        l = setData(book, file.getAbsolutePath(), leyendo);

        queryRecord.setNewBook(l);

        return l.getCopyBookUrl();
    }


    private Libro setData (Book b,String absolutePath, boolean leyendo){
        Libro l = new Libro();

        try {
            l.setTitle(b.getTitle());
            l.setAuthor(b.getMetadata().getAuthors().get(0).getFirstname() + " " + b.getMetadata().getAuthors().get(0).getLastname());
            //l.setSerie();
            l.setLanguage(b.getMetadata().getLanguage());
            l.setIdentifier(b.getMetadata().getIdentifiers().get(0).getValue());
            l.setOriginalBookUrl(absolutePath);
            //l.setFormat(b.getMetadata().getFormat());
            l.setFormat("EPUB");
            l.setLeyendo(leyendo);

            l.setReadProgress((float) 0);
            l.setLastPage(0);

            l.setCopyBookUrl(createBook(b));

            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeByteArray(b.getCoverImage().getData(), 0, b.getCoverImage().getData().length);
            } catch (IOException e) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);
                //e.printStackTrace();
            }
            l.setImg(new BitmapManager().bitemapCompress(bitmap));
        }catch (Exception e){
            l = null;
        }
        return l;
    }
//------ copia de libros en la carpeta personal del programa -------------------------------------------------------------------------------------

    // Copia los nuevos epubs analizados y los pega en "coleccionLibros"
    // dentro del direcctorio personal del programa
    // devuelve la ruta de la copia
    private String createBook(Book libro){
        File bookCollection = new File(context.getFilesDir(),"bookCollection");

        if (!bookCollection.exists()){
            bookCollection.mkdir();
        }

        File libroCopy =  new File(bookCollection, libro.getMetadata().getIdentifiers().get(0).getValue()+".epub");

        if (!libroCopy.exists()){
            try {
                EpubWriter epubWriter = new EpubWriter();
                epubWriter.write(libro, new FileOutputStream(libroCopy.getAbsolutePath()));
                System.out.println(libro.getTitle()+"\n\n");
            } catch (Exception e) {
            e.printStackTrace();
            }
        }

        return libroCopy.getAbsolutePath();
    }

    public void removeBook(String path){
        File file = new File(path);
        file.delete();
    }

    public File inputStreamToFile(InputStream inputStream, String fileName) throws IOException {
        int CHUNK_SIZE = 1024 * 4;


        String path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        File file  = new File(path, fileName);

        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        byte[] chunk = new byte[CHUNK_SIZE];
        int bytesLeidos = 0;
        //mientras que podamos leer bytes del stream de entrada
        //en bloques de tamaño CHUNK_SIZE
        while ( (bytesLeidos = inputStream.read(chunk)) > 0) {
            //escribir los bytes leidos en el arreglo
            //desde la posición 0 hasta la posición marcada por
            //el valor de la variable bytesLeidos
            os.write(chunk, 0, bytesLeidos);
        }
        os.close();

        return file;
    }

    /*public void DownloadFromDB(String magnetLink, String nameFile){
        String path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
         File file  = new File(path, nameFile);

        MagnetDownloadHelper magnetDownloaderHelper = new MagnetDownloadHelper();
        magnetDownloaderHelper.download(magnetLink, file.getPath());

        InputStream inputStream = magnetDownloaderHelper.getInputStream();

        try {
            Book book = (new EpubReader()).readEpub(inputStream);
            File newFile = inputStreamToFile(inputStream, book.getMetadata().getIdentifiers().get(0).getValue()+".epub");
            file.delete();

            addLibroInDB(newFile);


        } catch (IOException e) {
            e.printStackTrace();
        }

        magnetDownloaderHelper.stop();
    }*/

}
