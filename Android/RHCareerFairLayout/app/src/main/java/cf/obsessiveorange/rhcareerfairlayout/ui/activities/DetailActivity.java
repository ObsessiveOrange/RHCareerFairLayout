package cf.obsessiveorange.rhcareerfairlayout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Category;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;

public class DetailActivity extends AppCompatActivity {

    long mCompanyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        DBManager.setupDBAdapterIfNeeded(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);

        }

        Intent data = getIntent();
        mCompanyId = data.getLongExtra(RHCareerFairLayout.INTENT_KEY_SELECTED_COMPANY, -1);

        if(mCompanyId == -1){
            throw new IllegalArgumentException("Invalid companyId provided");
        }

        Company company = DBManager.getCompany(mCompanyId);
        HashMap<String, ArrayList<Category>> categories = DBManager.getCategoriesForCompany(mCompanyId);

        if(company == null || categories == null){
            throw new IllegalArgumentException("No such company provided");
        }

        StringBuilder majors = new StringBuilder();
        for(Category major : categories.get(RHCareerFairLayout.KEY_CATEGORY_MAJOR)){
            majors.append(major.getName());
            majors.append(", ");
        }
        majors.delete(majors.length()-2, majors.length());

        StringBuilder positionTypes = new StringBuilder();
        for(Category positionType : categories.get(RHCareerFairLayout.KEY_CATEGORY_POSITION_TYPE)){
            positionTypes.append(positionType.getName());
            positionTypes.append(", ");
        }
        positionTypes.delete(positionTypes.length()-2, positionTypes.length());

        StringBuilder workAuthorizations = new StringBuilder();
        for(Category workAuthorization : categories.get(RHCareerFairLayout.KEY_CATEGORY_WORK_AUTHORIZATION)){
            workAuthorizations.append(workAuthorization.getName());
            workAuthorizations.append(", ");
        }
        workAuthorizations.delete(workAuthorizations.length()-2, workAuthorizations.length());

        TextView detailName = (TextView) findViewById(R.id.detail_companyName);
        TextView detailWebsite = (TextView) findViewById(R.id.detail_companyWebsite);
        TextView detailDescription = (TextView) findViewById(R.id.detail_description);
        TextView detailMajors = (TextView) findViewById(R.id.detail_majors);
        TextView detailPositionTypes = (TextView) findViewById(R.id.detail_positionTypes);
        TextView detailWorkAuthorizations = (TextView) findViewById(R.id.detail_workAuthorizations);
        TextView detailAddress = (TextView) findViewById(R.id.detail_address);

        detailName.setText(company.getName());

        if(company.getWebsiteLink() == null || company.getWebsiteLink().isEmpty()){
            detailWebsite.setVisibility(View.GONE);
        }
        else{
            detailWebsite.setText(company.getWebsiteLink());
        }

        if(company.getAddress() == null || company.getAddress().isEmpty()){
            detailAddress.setVisibility(View.GONE);
            findViewById(R.id.detail_address_header).setVisibility(View.GONE);
            findViewById(R.id.detail_address_spacer).setVisibility(View.GONE);
        }
        else{
            detailAddress.setText(company.getAddress());
        }

        if(company.getDescription() == null || company.getDescription().isEmpty()){
            detailDescription.setVisibility(View.GONE);
            findViewById(R.id.detail_description_header).setVisibility(View.GONE);
            findViewById(R.id.detail_description_spacer).setVisibility(View.GONE);
        }
        else{
            detailDescription.setText(company.getDescription());
        }
        detailMajors.setText(majors);
        detailPositionTypes.setText(positionTypes);
        detailWorkAuthorizations.setText(workAuthorizations);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.close_btn:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.viewOnMap_btn:
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RHCareerFairLayout.INTENT_KEY_SELECTED_COMPANY, mCompanyId);
                setResult(RESULT_OK, returnIntent);
                finish();
                break;

            default:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return true;
    }
}
