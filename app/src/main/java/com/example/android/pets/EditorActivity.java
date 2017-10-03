/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

import java.net.URI;

/**
 * Allows user to create a new pet or edit an existing one.
 * Permite ao usuário criar um novo animal de estimação ou editar um existente.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * Identifier for the pet data loader
     * Identificador para o carregador de dados para animais de estimação */
    private static final int EXISTING_PET_LOADER = 0;
    /**
     * URI de Conteúdo para o pet existente (nulo se é um novo pet)
     * Conteúdo URI para o animal de estimação existente (nulo se é um novo animal de estimação) */
    private Uri mCurrentPetUri;
    /**
     * EditText field to enter the pet's name
     * Campo EditText para inserir o nome do animal de estimação
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     * Campo EditText para inserir a raça do animal de estimação
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     * Campo EditText para inserir o peso do animal de estimação
     */
    private EditText mWeightEditText;
    /**
     * Gender of the pet. The possible valid values are in the PetContract.java file:
     * Sexo do animal de estimação. Os possíveis valores válidos estão no arquivo PetContract.java:
     *
     * {@link PetEntry#GENDER_UNKNOWN}, {@link PetEntry#GENDER_MALE}, or
     * {@link PetEntry#GENDER_FEMALE}.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    /**
     * EditText field to enter the pet's gender
     * Campo EditText para inserir o gênero do animal de estimação
     */
    private Spinner mGenderSpinner;

    /**
     * Database helper that will provide us access to the database
     * Database helper que nos dará acesso ao banco de dados */
    private PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.

        // Examine a intenção que foi usada para iniciar esta atividade,
        // para descobrir se estamos criando um novo animal de estimação ou editando um existente.
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet.

        // Se a intenção NÃO contiver URI de conteúdo para animais de estimação, então sabemos que somos
        // criando um novo animal de estimação.
        if (mCurrentPetUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            // Este é um novo animal de estimação, então altere a barra do aplicativo para dizer "Adicionar um animal de estimação"
            setTitle(getString(R.string.editor_activity_title_new_pet));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)

            // Invalide o menu de opções, então a opção de menu "Excluir" pode ser oculta.
            // (Não faz sentido excluir um animal de estimação que ainda não foi criado).
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            // Caso contrário, este é um animal de estimação existente, então altere a barra de
            // aplicativos para dizer "Editar animais de estimação"
            setTitle(getString(R.string.editor_activity_title_edit_pet));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor

            // Inicialize um carregador para ler os dados do animal de estimação do banco de dados
            // e exibir os valores atuais no editor
            getSupportLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        // Encontre todas as visualizações relevantes que precisaremos para ler a entrada do usuário de
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new PetDbHelper(this);



        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     * Configure o spinner suspenso que permite ao usuário selecionar o gênero do animal de estimação.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout

        // Crie um adaptador para spinner. As opções de lista são da matriz String que usará
        // o spinner usará o layout padrão
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        // Especifique o estilo de layout suspenso - exibição de lista simples com 1 item por linha
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        // Aplique o adaptador ao girador
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        // Define o inteiro mSeleccionado para os valores constantes
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            // Como AdapterView é uma classe abstrata, onNothingSelected deve ser definido
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetContract.PetEntry.GENDER_UNKNOWN;; // Unknown
            }
        });
    }

    /**
     * Obtém entrada do usuário do editor e salva o novo pet no banco de dados.
     */
    private void insertPet() {
        // Lê dos campos de entrada
        // Use trim para eliminar espaços em branco à direita e à esquerda
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weight = Integer.parseInt(weightString);

        // Cria um objeto ContentValues onde nomes de coluna são as chaves,
        // e atributos de pet do editor são os valores.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        // Insere um novo pet no provider, returnando o URI de conteúdo para o novo pet.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

        // Mostra um mensagem toast dependendo ou não se a inserção foi bem sucedida
        if (newUri == null) {
            // Se o novo conteúdo do URI é nulo, então houve um erro com inserção.
            Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Caso contrário, a inserção foi bem sucedida e podemos mostrar um toast.
            Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                insertPet();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Como o editor mostra todos os atributos de pet, defina uma projeção que contenha
        // todas as colunas da tabela pet
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };

        // Este loader executará o método de query do ContentProvider em uma thread de segundo plano
        return new CursorLoader(this,   // Contexto da activity Pai
                mCurrentPetUri,         // Busca o URI de conteúdo para o pet corrente
                projection,             // Colunas para incluir no Cursor resultante
                null,                   // Sem cláusula de selection
                null,                   // Sem selection args
                null);                  // Ordem de seleção padrão
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Procede movendo o primeiro registro do cursor e lendo data dele
        // (Esta deveria ser o único registro do cursor)
        if (cursor.moveToFirst()) {
            // Acha as colunas de atributos pet em que estamos interessados
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // Extrai o valor do Cursor para o índice de coluna dado
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            // Atualize as views na tela com os valores do banco de dados
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            // Gênero é um spinner dropdown, então mapeie o valor da constante do banco de dados
            // em uma das opções de dropdown (0 é Desconhecida, 1 é Masculino, 2 é Feminino).
            // Então chame setSelection() para que a opção seja mostrada na tela como a seleção corrente.
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}