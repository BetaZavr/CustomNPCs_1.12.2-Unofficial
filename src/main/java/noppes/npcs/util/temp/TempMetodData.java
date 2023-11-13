package noppes.npcs.util.temp;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.google.common.collect.Maps;

public class TempMetodData {
	
	public boolean isDeprecated, isStatic;
	public String name, typeS;
	public Map<String, String> parametersS;
	public Map<String, Class<?>> parameters;
	public Class<?> type;
	
	public TempMetodData(String metod, boolean isDeprecated) {
		this.parameters = Maps.<String, Class<?>>newLinkedHashMap();
		this.parametersS = Maps.<String, String>newLinkedHashMap();
		if (metod.indexOf("abstract")!=-1) { metod = metod.substring(metod.indexOf("abstract")+9); }
		int s = metod.lastIndexOf(" ", metod.indexOf("("));
		if (s<0) { s = 0; } else { s++;}
		this.isDeprecated = isDeprecated;
		this.isStatic = false;
		this.type = null;
		this.typeS = metod.substring(0, metod.indexOf(" "));
		this.name = metod.substring(s, metod.indexOf("("));
		if (metod.lastIndexOf("()")==-1 || metod.lastIndexOf("()") < metod.length()-2) {
			metod = metod.substring(metod.indexOf("(")+1, metod.lastIndexOf(")"));
			if (metod.indexOf("<")!=-1) {
				while (metod.indexOf("<")!=-1) {
					int g = metod.indexOf("<"), e = metod.indexOf("> ", metod.indexOf("<"));
					if (e==-1) { e = metod.length() - 1; } else { e++; }
					metod = metod.substring(0, g) + metod.substring(e);
				}
			}
			String[] prms = metod.split(", ");
			for (int p = 0; p < prms.length; p++) {
				this.parametersS.put(prms[p].substring(prms[p].lastIndexOf(" ")+1), prms[p].substring(0, prms[p].lastIndexOf(" ")));
			}
		}
	}

	public TempMetodData(Method m) {
		this.name = m.getName();
		this.type = m.getReturnType();
		this.typeS = this.type.getSimpleName();
		this.parameters = Maps.<String, Class<?>>newLinkedHashMap();
		this.parametersS = Maps.<String, String>newLinkedHashMap();
		for (Parameter p : m.getParameters()) {
			this.parameters.put(p.getName(), p.getType());
			this.parametersS.put(p.getName(), p.getType().getSimpleName());
		}
		this.isStatic = Modifier.isStatic(m.getModifiers());
		this.isDeprecated = false;
		for (Annotation annt : m.getDeclaredAnnotations()) {
			if (annt.annotationType()==Deprecated.class) {
				this.isDeprecated = true;
				break;
			}
		}
	}

	public boolean comparison(TempMetodData tdm) {
		if (this.name==null || tdm.name==null || tdm.name.isEmpty() || !this.name.equals(tdm.name)) { return false; }
		if (!this.typeS.equals(tdm.typeS)) { return false; }
		if (this.parametersS.size() != tdm.parametersS.size()) { return false; }
		String[] tprs = this.parametersS.values().toArray(new String[this.parametersS.size()]);
		String[] qprs = tdm.parametersS.values().toArray(new String[tdm.parametersS.size()]);
		for (int i = 0; i < tprs.length; i++) {
			if (!tprs[i].equals(qprs[i])) { return false; }
		}
		if (this.isStatic!=tdm.isStatic || this.isDeprecated!=tdm.isDeprecated) { return false; }
		return true;
	}

	public void setData(TempMetodData tdm) {
		String[] qprs = tdm.parametersS.keySet().toArray(new String[tdm.parametersS.size()]);
		Map<String, Class<?>> map = Maps.newLinkedHashMap();
		int i = 0;
		for (String key : this.parameters.keySet()) {
			map.put(qprs[i], this.parameters.get(key));
			i++;
		}
		this.parameters = map;
	}

	public static String getCode(Class<?> c) {
		if (c == null) { return "null"; }
		String code = c.getSimpleName();
		String name = code;

		if (c.isArray()) {
			String path = c.getTypeName();
			path = path.substring(0, path.indexOf("[]"));
			try { c = Class.forName(path); } catch (Exception e) {}
			code = c.getSimpleName();
		}
		if (code.indexOf("[]")!=-1) { code = code.substring(0, code.indexOf("[]")); }
		
		if (c.getName().startsWith("java.") || c.getName().startsWith("javax.")) {
			String path = c.getName().replace("."+code, "/");
			while(path.indexOf(".")!=-1) { path = path.replace(".", "/"); }
			return "[https://docs.oracle.com/javase/8/docs/api/"+path.toLowerCase()+code+".html "+name+"]";
		}
		
		try { // try any java type
			String path = (""+code.charAt(0)).toUpperCase()+code.substring(1);
			if (code.toLowerCase().equals("int")) { path = "Integer"; }
			Class<?> l = Class.forName("java.lang."+path);
			if (l != null) {
				return "[https://docs.oracle.com/javase/8/docs/api/java/lang/"+path+".html "+name+"]";
			}
		} catch (Exception e) {}
		
		if (TempClass.tempMap.containsKey(c)) {
			TempDataClass tmd = TempClass.tempMap.get(c);
			return "[[Custom NPCs/Unoficial_API_1.12.2/"+tmd.path+"/"+code+"|"+name+"]]";
		}
		if (c.getName().startsWith("net.minecraft.") || c.getName().startsWith("net.minecraftforge.")) {
			String path = c.getName().replace("."+code, "/");
			while(path.indexOf(".")!=-1) { path = path.replace(".", "/"); }
			return "[https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.12.2/"+path.toLowerCase()+code+".html "+name+"]";
		}
		if (c.getName().startsWith("noppes.npcs.")) {
			String path = c.getName().replace("."+code, "/");
			while(path.indexOf(".")!=-1) { path = path.replace(".", "/"); }
			return "[https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/tree/master/src/main/java/"+path.toLowerCase()+code+".java "+name+"]";
		}
		
		for (Class<?> inrf : TempClass.fulls.keySet()) {
			for (Class<?> cl : TempClass.fulls.get(inrf)) {
				if (cl!=c || !TempClass.tempMap.containsKey(inrf)) { continue; }
				TempDataClass tmd = TempClass.tempMap.get(inrf);
				code = inrf.getSimpleName();
				return "[[Custom NPCs/Unoficial_API_1.12.2/"+tmd.path+"/"+code+"|"+code+"]]";
			}
		}

		//System.out.println("Class: "+c.getName()+"; Code: \""+code+"\"; Name: \""+name+"\" == "+TempClass.tempMap.containsKey(c));
		
		return name;
	}

	public String getMetodBody(TempDataClass tdc) {
		String code = "[[Custom NPCs/Unoficial_API_1.12.2/"+tdc.path+"/"+tdc.api.getSimpleName()+"#"+this.name+"|"+this.name+"]](";
		if (!this.parameters.isEmpty()) {
			String pmtrs = "";
			for (String key : this.parameters.keySet()) {
				if (!pmtrs.isEmpty()) { pmtrs += ", "; }
				pmtrs += TempMetodData.getCode(this.parameters.get(key)) + " " +key;
			}
			code += pmtrs;
		}
		code += ")";
		return code;
	}

	public String getMetodKey(TempDataClass tdc) {
		String path = tdc.path;
		while(path.indexOf("/")!=-1) { path = path.replace("/", "."); }
		return "method."+path+"."+tdc.api.getSimpleName().toLowerCase()+"."+this.name;
	}
	
	public String getParametrKey(TempDataClass tdc, String name) {
		String path = tdc.path;
		while(path.indexOf("/")!=-1) { path = path.replace("/", "."); }
		return "parametr."+path+"."+tdc.real.getSimpleName().toLowerCase()+"."+this.name+"."+name;
	}
	
}
