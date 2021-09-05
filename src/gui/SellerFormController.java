package gui; //CLASSE PRA CONTROLAR O SELLERFORM 

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentServices;
import model.services.SellerServices;

public class SellerFormController implements Initializable {

	private SellerServices services;

	private DepartmentServices departmentServices;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	private Seller entity;

	@FXML
	private TextField txtid;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtEmail;
	@FXML
	private DatePicker dpBrintDate;
	@FXML
	private TextField txtBaseSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorEmail;
	@FXML
	private Label labelErrorBirthDate;
	@FXML
	private Label labelErrorBaseSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	@FXML
	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerServices services, DepartmentServices departmentServices) {
		this.services = services;
		this.departmentServices = departmentServices;

	}

	public void SubscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	private void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("ENTITY IS NULL");
		}
		if (services == null) {
			throw new IllegalStateException("SEVICES IS NULL");
		}

		try {
			entity = getFormData();
			services.saverOrUpdate(entity);
			notifyDataListeners();
			Utils.currenteStage(event).close();
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("ERRO SAVING OBJECT", null, e.getMessage(), AlertType.ERROR);
		}

	}

	private void notifyDataListeners() {
		for (DataChangeListener listeners : dataChangeListeners) {
			listeners.onDataChange();
		}
	}

	private Seller getFormData() {
		Seller obj = new Seller();

		ValidationException exception = new ValidationException("validation error");

		obj.setId(Utils.tryParseToInt(txtid.getText()));

		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "field can't be empty");
		}
		
		
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addError("email", "field can't be empty");
		}
		obj.setEmail(txtEmail.getText());
		
		
		if(dpBrintDate.getValue() == null) {
			exception.addError("birthDate", "field can't be empty");
		}
		else {
		Instant instant = Instant.from(dpBrintDate.getValue().atStartOfDay(ZoneId.systemDefault()));
		obj.setBirthDate(Date.from(instant));
		}
		
		
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			exception.addError("baseSalary", "field can't be empty");
		}
		obj.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));
		
		
		obj.setDepartment(comboBoxDepartment.getValue());

		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		obj.setName(txtName.getText());

		return obj;

	}

	@FXML
	private void onBtCancelAction(ActionEvent event) {
		Utils.currenteStage(event).close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializaNodes();
	}

	private void initializaNodes() {
		Constraints.setTextFieldInteger(txtid);
		Constraints.setTextFieldMaxLength(txtName, 30);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBrintDate, "dd/MM/yyyy");
		initializeComboBoxDepartment();

	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("ERROR ENTITY IS  NULL");
		}

		txtid.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));
		if (entity.getBirthDate() != null) {
			dpBrintDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		
		if(entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		}else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}
		
	}

	public void loadAssociatedObjects() { // CRIAR UMA LISTA DE DEPARTMENTS NO SELLER
		if (departmentServices == null) {
			throw new IllegalStateException("DEPARTMENTSERVICES IS NULL");
		}

		List<Department> list = departmentServices.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);

	}

	private void setErrorMessages(Map<String, String> error) {
		Set<String> fields = error.keySet();

		//OPERADOR CONDICIONAL 
		labelErrorName.setText(fields.contains("name") ? error.get("name") : "" );
		labelErrorEmail.setText(fields.contains("email") ? error.get("email") : "" );
		labelErrorBaseSalary.setText(fields.contains("baseSalary") ? error.get("baseSalary") : "" );
		labelErrorBirthDate.setText(fields.contains("birthDate") ? error.get("birthDate") : "" );
		
		
		
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}
