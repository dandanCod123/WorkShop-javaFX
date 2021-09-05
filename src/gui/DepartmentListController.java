package gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Department;
import model.services.DepartmentServices;

public class DepartmentListController implements Initializable, DataChangeListener {

	// CRIAR REFERENCIAS PARA O "DepartmenteList.fxml"
	// ESSA CLASSE SERÁ PARA ESTABELECER UMA RELAÇÃO E CONTROLE DO
	// DEPARTMENTLIST.FXML

	private DepartmentServices services; // INSTANCIAMOS DEPARTMENTESERVICES

	// INTENS PARA REGISTRAR ID E NAME
	@FXML
	private TableView<Department> TableViewDepartment;
	@FXML
	private TableColumn<Department, Integer> TableColumId;
	@FXML
	private TableColumn<Department, String> TableColumName;

	@FXML
	private TableColumn<Department, Department> tableColumnEDIT;

	@FXML
	private TableColumn<Department, Department> tableColumnREMOVE;

	@FXML
	private Button btNew;
	// END

	// METODOS DE CONTROLE DO DEPARTMENT

	private ObservableList<Department> obsList;

	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currenteStage(event);
		Department obj = new Department();
		creatDialogForm(obj, "/gui/DepartmentForm.fxml", parentStage);

	}

	public void setDepartmentServices(DepartmentServices services) {
		this.services = services;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeNode();
	}

	private void initializeNode() {
		TableColumId.setCellValueFactory(new PropertyValueFactory<>("id"));
		TableColumName.setCellValueFactory(new PropertyValueFactory<>("name"));

		Stage stage = (Stage) Main.getMainScene().getWindow(); // FAZER COM QUE A TABELA SIGA ATE O FINAL DA TELA
		TableViewDepartment.prefHeightProperty().bind(stage.heightProperty());
	}

	public void UpadateTableView() { // ATUALIZAR A LISTA DE DEPARTMENTS
		if (services == null) {
			throw new IllegalAccessError("Services is null");
		}
		List<Department> list = services.findAll();
		obsList = FXCollections.observableArrayList(list);
		TableViewDepartment.setItems(obsList);
		initEditButtons();
		initRemoveButtons();

	}

	// AULA 281

	private void creatDialogForm(Department obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			DepartmentFormController controller = loader.getController();
			controller.setDepartment(obj);
			controller.setDepartmentServices(new DepartmentServices());
			controller.SubscribeDataChangeListener(this);
			controller.updateFormData();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("ERRO DEPARTMENT DATA");
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false);
			dialogStage.initOwner(parentStage);
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();

		} catch (IOException e) {
			Alerts.showAlert("IOEXECEPTION", "ERROR LOADING VIEW", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChange() {
		UpadateTableView();
	}

	private void initEditButtons() { // VAI CRIAR UM BUTTON PARA EDITAR O DEPARTMENTO, CRIANOD A PARTIR DIRETAMENTE
										// NO CODIGO
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Department obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
						event -> creatDialogForm(obj, "/gui/DepartmentForm.fxml", Utils.currenteStage(event)));
			}
		});
	}

	private void initRemoveButtons() { // INSTANCIA UM BUTTON PARA DELETAR OS DEPARTMENTS, DIRETAMENTE NO CODIGO 
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Department obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	private void removeEntity(Department obj) {
      Optional<ButtonType> result = Alerts.showConfirmation("confimation", "ARE YOU SURE TO DELETE?");
      
      if(result.get() == ButtonType.OK) { //VERIFICA SE A PESSOA CLICOU NO BOTÃO OK DA TELA
    	  if(services == null) {
    		  throw new IllegalStateException("SERVICES IS NULL");
    	  }
    	  try {
    	  services.Remove(obj); // SE ELE CLICOU NO OK, REMOVA O DEPARTMENT
    	  UpadateTableView();
    }
    	  catch(DbIntegrityException e) {
    		  Alerts.showAlert("ERROR REMOVE OBECT", null, e.getMessage(), AlertType.ERROR);
    	  }
    	  
   }
	}

}
