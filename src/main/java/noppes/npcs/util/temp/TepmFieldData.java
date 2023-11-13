package noppes.npcs.util.temp;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TepmFieldData {
	
	public boolean isDeprecated = false;
	public Field field;
	public String name;
	public Class<?> type;
	public boolean isStatic;

	public TepmFieldData(Field f) {
		this.field = f;
		this.name = f.getName();
		this.type = f.getType();
		this.isStatic = Modifier.isStatic(f.getModifiers());
		this.isDeprecated = false;
		for (Annotation annt : f.getDeclaredAnnotations()) {
			if (annt.annotationType()==Deprecated.class) {
				this.isDeprecated = true;
				break;
			}
		}
	}
	
	public String getFieldKey(TempDataClass tdc) {
		String path = tdc.path;
		while(path.indexOf("/")!=-1) { path = path.replace("/", "."); }
		return "field."+path+"."+tdc.api.getSimpleName().toLowerCase()+"."+this.name;
	}
	
}
