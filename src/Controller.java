import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class Controller {

	public static void main(String[] args) {

		//opens a window to select the maze file
		JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		int returnVal = fileChooser.showOpenDialog(null);
		
		//if user opens file
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			
			//starts the main functionality with the chosen file
			new Model(file);
		}
		
	}

}
