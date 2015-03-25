import apa.*;

public interface Computere extends Computer{
	public default int getCFValue(){
		return 1;
	}

}
