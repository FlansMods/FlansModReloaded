package com.flansmod.common.types;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.VecWithOverride;
import com.flansmod.util.Maths;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DefinitionParser
{
	private Class<? extends JsonDefinition> classRef;
	
	private enum FieldType
	{
		INT, DOUBLE, FLOAT, BYTE, SHORT, STRING, STRUCT, LIST;
	}

	private interface FieldParseMethod {
		Object Parse(Object ref, JsonElement jNode, JsonField annotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
	}

	private static HashMap<Type, FieldParseMethod> Parsers = new HashMap<>();
	static
	{
		Parsers.put(Integer.TYPE, (ref, jNode, annot) -> { return Maths.Clamp(jNode.getAsInt(), Maths.Ceil(annot.Min()), Maths.Floor(annot.Max())); });
		Parsers.put(Float.TYPE, (ref, jNode, annot) -> { return Maths.Clamp(jNode.getAsFloat(), (float)annot.Min(), (float)annot.Max()); });
		Parsers.put(Double.TYPE, (ref, jNode, annot) -> { return Maths.Clamp(jNode.getAsDouble(), annot.Min(), annot.Max()); });
		Parsers.put(Short.TYPE, (ref, jNode, annot) -> { return Maths.Clamp(jNode.getAsShort(), (short)annot.Min(), (short)annot.Max()); });
		Parsers.put(Byte.TYPE, (ref, jNode, annot) -> { return jNode.getAsByte(); });
		Parsers.put(Boolean.TYPE, (ref, jNode, annot) -> { return jNode.getAsBoolean(); });
		Parsers.put(String.class, (ref, jNode, annot) -> { return jNode.getAsString(); });
		Parsers.put(Vec3.class, (ref, jNode, annot) ->
		{
			JsonArray jVec = jNode.getAsJsonArray();
			return new Vec3(jVec.get(0).getAsDouble(),
				jVec.get(1).getAsDouble(),
				jVec.get(2).getAsDouble());
		});
		Parsers.put(Vector3f.class, (ref, jNode, annot) ->
		{
			JsonArray jVec = jNode.getAsJsonArray();
			return new Vector3f(jVec.get(0).getAsFloat(),
				jVec.get(1).getAsFloat(),
				jVec.get(2).getAsFloat());
		});
		Parsers.put(VecWithOverride.class, VecWithOverride::ParseFunc);

	}
	private static FieldParseMethod GetParserFor(Field field) { return GetParserFor(field.getType()); }
	private static FieldParseMethod GetParserFor(Type type)
	{
		/*if(type instanceof Class<?> classType)
		{
			if(classType.isArray())
			{
				Type elementType = classType.componentType();
				FieldParseMethod elementParser = GetParserFor(elementType);
				if(elementParser != null)
					return new ListDeserializer(elementParser);
			}
		}*/
		return Parsers.get(type);
	}

	public static void IterativelyCreateParsers(Type type)
	{
		if(GetParserFor(type) != null)
			return;
		if(type instanceof Class classRef)
		{
			// Watch out for infinite loops, put a null parser in
			Parsers.put(classRef, null);

			if(classRef.isEnum())
			{
				Parsers.put(classRef, new EnumDeserializer(classRef));
			}
			else if(classRef.isArray())
			{
				Type elementType = classRef.componentType();
				IterativelyCreateParsers(elementType);
				Parsers.put(classRef, new ListDeserializer(GetParserFor(elementType), (Class)elementType));
			}
			else
			{
				Parsers.put(classRef, new ClassDeserializer(classRef));
				for (Field field : classRef.getFields())
				{
					JsonField annotation = field.getAnnotation(JsonField.class);
					if (annotation != null)
					{
						IterativelyCreateParsers(field.getType());
					}
				}
			}
		}
	}

	public static class EnumDeserializer<TEnum extends Enum<TEnum>> implements FieldParseMethod
	{
		private final Class<TEnum> enumRef;

		public EnumDeserializer(Class<TEnum> ref)
		{
			enumRef = ref;
		}

		@Override
		public Object Parse(Object ref, JsonElement jNode, JsonField annotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
		{
			try
			{
				return Enum.valueOf(enumRef, jNode.getAsString());
			}
			catch (Exception e)
			{
				return Enum.valueOf(enumRef, jNode.getAsString().toLowerCase());
			}
		}
	}

	public static class ListDeserializer implements FieldParseMethod
	{
		private final FieldParseMethod elementParser;
		private final Class elementType;

		public ListDeserializer(FieldParseMethod parser, Class type)
		{
			elementParser = parser;
			elementType = type;
		}

		@Override
		public Object Parse(Object ref, JsonElement jNode, JsonField annotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
		{
			JsonArray jArray = jNode.getAsJsonArray();
			if(jArray != null)
			{
				Object[] array = (Object[])Array.newInstance(elementType, jArray.size());
				int index = 0;
				for(JsonElement jElement : jArray)
				{
					array[index] = elementParser.Parse(null, jElement, annotation);
					index++;
				}
				return array;
			}
			return new Object[0];
		}
	}

	public static class ClassDeserializer<TClass> implements FieldParseMethod
	{
		private final HashMap<String, Field> memberFields;
		private final Class<? extends TClass> classRef;

		public ClassDeserializer(Class<? extends TClass> cl)
		{
			classRef = cl;
			memberFields = new HashMap<>();
			for(Field field : classRef.getFields())
			{
				JsonField annotation = field.getAnnotation(JsonField.class);
				if(annotation != null)
				{
					memberFields.put(field.getName(), field);
				}
			}
		}

		@Override
		public Object Parse(Object ref, JsonElement jNode, JsonField annotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
		{
			if(ref == null)
				ref = classRef.getDeclaredConstructor().newInstance();

			for (var kvp : jNode.getAsJsonObject().entrySet())
			{

				Field targetField = memberFields.get(kvp.getKey());
				if(targetField != null)
				{
					FieldParseMethod parser = GetParserFor(targetField);
					if(parser != null)
					{
						Object targetObject = targetField.get(ref);
						targetObject = parser.Parse(targetObject, kvp.getValue(), targetField.getAnnotation(JsonField.class));
						targetField.set(ref, targetObject);
					}
				}
			}

			return ref;
		}
	}

	public static boolean LoadFromJSON(JsonDefinition definition, JsonElement jRoot)
	{
		try
		{
			if (definition == null || jRoot == null || !jRoot.isJsonObject())
				return false;

			FieldParseMethod parser = GetParserFor(definition.getClass());
			if(parser == null)
				return false;

			parser.Parse(definition, jRoot, null);

			// Also check for a custom JSON Load func
			definition.LoadExtra(jRoot);

			return true;
		}
		catch(Exception e)
		{
			FlansMod.LOGGER.error("Failed to load a definition");
			FlansMod.LOGGER.error(e.getMessage());
			return false;
		}
	}















/*



	private HashMap<String, FieldParseInfo> autoFields;
	private Object ParseNode(FieldParseInfo parseInfo, JsonElement jNode)
		throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException
	{
		JsonField annot = parseInfo.annotation;
		switch(parseInfo.type)
		{
			case INT: 			return Maths.Clamp(jNode.getAsInt(), Maths.Ceil(annot.Min()), Maths.Floor(annot.Max()));
			case FLOAT: 		return Maths.Clamp(jNode.getAsFloat(), (float)annot.Min(), (float)annot.Max());
			case DOUBLE: 		return Maths.Clamp(jNode.getAsDouble(), annot.Min(), annot.Max());
			case BYTE: 			return jNode.getAsByte();
			case SHORT: 		return Maths.Clamp(jNode.getAsShort(), (short)annot.Min(), (short)annot.Max());
			case STRING: 		return jNode.getAsString();

			default:			return null;
		}
	}
	
	private static class FieldParseInfo
	{
		public Field targetField;
		public JsonField annotation;
		public FieldType type;
		public Type listElementType;
		public HashMap<String, FieldParseInfo> structFieldInfos;
		
		public boolean HasFields() { return structFieldInfos != null && !structFieldInfos.isEmpty(); }
		public boolean IsPrimitive() { return targetField.getType().isPrimitive(); }
		
		public FieldParseInfo(Field f, JsonField a)
		{
			targetField = f;
			annotation = a;
			
			if(targetField.getType().equals(String.class))
				type = FieldType.STRING;
			else if(targetField.getType().equals(Integer.TYPE))
				type = FieldType.INT;
			else if(targetField.getType().equals(Float.TYPE))
				type = FieldType.FLOAT;
			else if(targetField.getType().equals(Double.TYPE))
				type = FieldType.DOUBLE;
			else if(targetField.getType().equals(Short.TYPE))
				type = FieldType.SHORT;
			else if(targetField.getType().equals(Byte.TYPE))
				type = FieldType.BYTE;
			else if(List.class.isAssignableFrom(targetField.getType()))
			{
				type = FieldType.LIST;
				listElementType = ((ParameterizedType)targetField.getGenericType()).getActualTypeArguments()[0];
			}
			else
				type = FieldType.STRUCT;
			
			if(!targetField.getType().isPrimitive())
			{
				structFieldInfos = new HashMap<>();
				for(Field field : f.getType().getFields())
				{
					JsonField annotation = field.getAnnotation(JsonField.class);
					if(annotation != null)
					{
						structFieldInfos.put(field.getName(), new FieldParseInfo(field, annotation));
					}
				}
			}
		}
	}
	
	public DefinitionParser(Class<? extends JsonDefinition> type)
	{
		classRef = type;
		autoFields = new HashMap<>();
		for(Field field : type.getFields())
		{
			JsonField annotation = field.getAnnotation(JsonField.class);
			if(annotation != null)
			{
				autoFields.put(field.getName(), new FieldParseInfo(field, annotation));
			}
		}
	}
	

	
	private Object ParseObject(Object struct, FieldParseInfo parseInfo, JsonElement jNode)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
	{
		// Create a new struct if there is none
		if(struct == null)
		{
			struct = parseInfo.targetField.getType().getDeclaredConstructor().newInstance();
		}
		// Then parse json nodes
		JsonObject jObj = jNode.getAsJsonObject();
		for(var kvp : parseInfo.structFieldInfos.entrySet())
		{
			LoadField(struct, kvp.getValue(), jObj.get(kvp.getKey()));
		}
		return struct;
	}
	
	private void LoadField(Object target, FieldParseInfo parseInfo, JsonElement jNode)
			throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, NullPointerException
	{
		JsonField annot = parseInfo.annotation;
		switch(parseInfo.type)
		{
			case INT:
				parseInfo.targetField.setInt(target, (int)ParseNode(parseInfo, jNode));
				return;
			case FLOAT:
				parseInfo.targetField.setFloat(target, (float)ParseNode(parseInfo, jNode));
				return;
			case DOUBLE:
				parseInfo.targetField.setDouble(target, (double)ParseNode(parseInfo, jNode));
				return;
			case BYTE:
				parseInfo.targetField.setByte(target, (byte)ParseNode(parseInfo, jNode));
				return;
			case SHORT:
				parseInfo.targetField.setShort(target, (short)ParseNode(parseInfo, jNode));
				return;
			case STRING:
				parseInfo.targetField.set(target, ParseNode(parseInfo, jNode));
				return;
			case STRUCT:
				parseInfo.targetField.set(target, ParseObject(parseInfo.targetField.get(target), parseInfo, jNode));
				return;
			case LIST:
				List list = (List)parseInfo.targetField.get(target);
				// Create a new zero-entry list if there is none
				if(list == null)
				{
					list = (List)parseInfo.targetField.getType().getDeclaredConstructor().newInstance();
					parseInfo.targetField.set(target, list);
				}
				// Then add an element for each json entry
				JsonArray jArray = jNode.getAsJsonArray();
				for(var jChild : jArray)
				{
					switch(parseInfo.listElementFieldInfo)
					{
						case INT:
						case FLOAT:
						case DOUBLE:
							list.add(ParseNode(parseInfo.listElementFieldInfo, jChild));
							break;
						case STRUCT:
							
							break;
						case LIST: // Invalid
						default:
							break;
					}
				}
				break;
		}
	}
	*/

}
