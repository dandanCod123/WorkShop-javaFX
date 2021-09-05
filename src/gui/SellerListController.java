package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
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
import model.entities.Seller;
import model.services.DepartmentServices;
import model.services.SellerServices;

public class SellerListController implements Initializable, DataChangeListener {

	// CRIAR REFERENCIAS PARA O "SellereList.fxml"
	// ESSA CLASSE SERÁ PARA ESTABELECER UMA RELAÇÃO E CONTROLE DO
	// DEPARTMENTLIST.FXML

	private SellerServices services; // INSTANCIAMOS DEPARTMENTESERVICES

	// INTENS PARA REGISTRAR ID E NAME
	@FXML
	private TableView<Seller> TableViewSeller;
	@FXML
	private TableColumn<Seller, Integer> TableColumId;
	@FXML
	private TableColumn<Seller, String> TableColumName;

	@FXML
	private TableColumn<Seller, String> TableColumEmail;
	
	@FXML
	private TableColumn<Seller, Date> TableColumBirthDate;
	
	@FXML
	private TableColumn<Seller, Double> TableColumBaseSalary;
	
	@FXML
	private TableColumn<Seller, Seller> tableColumnEDIT;

	@FXML
	private TableColumn<Seller, Seller> tableColumnREMOVE;

	@FXML
	private Button btNew;
	// END

	// METODOS DE CONTROLE DO DEPARTMENT

	private ObservableList<Seller> obsList;

	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currenteStage(event);
		Seller obj = new Seller();
		creatDialogForm(obj, "/gui/SellerForm.fxml", parentStage);

	}

	public void setSellerServices(SellerServices services) {
		this.services = services;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeNode();
	}

	private void initializeNode() {
		TableColumId.setCellValueFactory(new PropertyValueFactory<>("id"));
		TableColumName.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		TableColumBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		TableColumBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		Utils.formatTableColumnDate(TableColumBirthDate, "dd/MM/yyyy");
		Utils.formatTableColumnDouble(TableColumBaseSalary, 2);
		
		

		Stage stage = (Stage) Main.getMainScene().getWindow(); // FAZER COM QUE A TABELA SIGA ATE O FINAL DA TELA
		TableViewSeller.prefHeightProperty().bind(stage.heightProperty());
	}

	public void UpadateTableView() { // ATUALIZAR A LISTA DE DEPARTMENTS
		if (services == null) {
			throw new IllegalAccessError("Services is null");
		}
		List<Seller> list = services.findAll();
		obsList = FXCollections.observableArrayList(list);
		TableViewSeller.setItems(obsList);
		initEditButtons();
		initRemoveButtons();

	}

	// AULA 281

	private void creatDialogForm(Seller obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			SellerFormController controller = loader.getController();
			controller.setSeller(obj);
			controller.setServices(new SellerServices() , new DepartmentServices());
			controller.loadAssociatedObjects();
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
			e.printStackTrace();
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
		tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
						event -> creatDialogForm(obj, "/gui/SellerForm.fxml", Utils.currenteStage(event)));
			}
		});
	}

	private void initRemoveButtons() { // INSTANCIA UM BUTTON PARA DELETAR OS DEPARTMENTS, DIRETAMENTE NO CODIGO 
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
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

	private void removeEntity(Seller obj) {
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
