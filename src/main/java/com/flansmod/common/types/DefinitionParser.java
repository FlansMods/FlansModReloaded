package com.flansmod.common.types;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.VecWithOverride;
import com.flansmod.physics.common.util.Maths;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Optional;

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
		Parsers.put(Integer.TYPE, (ref, jNode, annot) ->
		{
			try { return Maths.Clamp(jNode.getAsInt(), Maths.Ceil(annot.Min()), Maths.Floor(annot.Max())); }
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as int due to exception: " + e); throw e; }
		});
		Parsers.put(Float.TYPE, (ref, jNode, annot) ->
		{
			try { return Maths.Clamp(jNode.getAsFloat(), (float)annot.Min(), (float)annot.Max()); }
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as float due to exception: " + e); throw e; }
		});
		Parsers.put(Double.TYPE, (ref, jNode, annot) ->
		{
			try { return Maths.Clamp(jNode.getAsDouble(), annot.Min(), annot.Max()); }
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as double due to exception: " + e); throw e; }
		});
		Parsers.put(Short.TYPE, (ref, jNode, annot) ->
		{
			try { return Maths.Clamp(jNode.getAsShort(), (short)annot.Min(), (short)annot.Max()); }
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as short due to exception: " + e); throw e; }
		});
		Parsers.put(Byte.TYPE, (ref, jNode, annot) ->
		{
			try { return jNode.getAsByte(); }
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as byte due to exception: " + e); throw e; }
		});
		Parsers.put(Boolean.TYPE, (ref, jNode, annot) ->
		{
			try { return jNode.getAsBoolean(); }
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as bool due to exception: " + e); throw e; }
		});
		Parsers.put(String.class, (ref, jNode, annot) ->
		{
			try { return jNode.getAsString(); }
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as string due to exception: " + e); throw e; }
		});
		Parsers.put(Vec3.class, (ref, jNode, annot) ->
		{
			try
			{
				JsonArray jVec = jNode.getAsJsonArray();
				return new Vec3(jVec.get(0).getAsDouble(),
					jVec.get(1).getAsDouble(),
					jVec.get(2).getAsDouble());
			}
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as Vec3 due to exception: " + e); throw e; }
		});
		Parsers.put(Vector3f.class, (ref, jNode, annot) ->
		{
			try
			{
				JsonArray jVec = jNode.getAsJsonArray();
				return new Vector3f(jVec.get(0).getAsFloat(),
					jVec.get(1).getAsFloat(),
					jVec.get(2).getAsFloat());
			}
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as Vector3f due to exception: " + e); throw e; }
		});
		Parsers.put(VecWithOverride.class, VecWithOverride::ParseFunc);
		Parsers.put(ResourceLocation.class, (ref, jNode, annot) -> {
			try
			{
				return new ResourceLocation(jNode.getAsString());
			}
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as ResourceLocation due to exception: " + e); throw e; }
		});

	}
	private static FieldParseMethod GetParserFor(Field field) { return GetParserFor(field.getType()); }
	private static FieldParseMethod GetParserFor(Type type)
	{
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
			catch (Exception e1)
			{
				try
				{
					return Enum.valueOf(enumRef, jNode.getAsString().toLowerCase());
				}
				catch (Exception e2)
				{
					try
					{
						return Enum.valueOf(enumRef, jNode.getAsString().toUpperCase());
					}
					catch (Exception e3)
					{
						FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as Enum<" + enumRef + "> due to exception: " + e3);
						throw e3;
					}
				}
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
			try
			{
				JsonArray jArray = jNode.getAsJsonArray();
				if (jArray != null)
				{
					Object array = Array.newInstance(elementType, jArray.size());
					if(array instanceof Object[] objArray)
					{
						int index = 0;
						for (JsonElement jElement : jArray)
						{
							objArray[index] = elementParser.Parse(null, jElement, annotation);
							index++;
						}
					}
					else if(array instanceof int[] intArray)
					{
						int index = 0;
						for (JsonElement jElement : jArray)
						{
							intArray[index] = (int)elementParser.Parse(null, jElement, annotation);
							index++;
						}
					}
					else if(array instanceof float[] floatArray)
					{
						int index = 0;
						for (JsonElement jElement : jArray)
						{
							floatArray[index] = (float)elementParser.Parse(null, jElement, annotation);
							index++;
						}
					}
					else if(array instanceof double[] doubleArray)
					{
						int index = 0;
						for (JsonElement jElement : jArray)
						{
							doubleArray[index] = (double)elementParser.Parse(null, jElement, annotation);
							index++;
						}
					}
					else if(array instanceof long[] longArray)
					{
						int index = 0;
						for (JsonElement jElement : jArray)
						{
							longArray[index] = (long)elementParser.Parse(null, jElement, annotation);
							index++;
						}
					}
					else if(array instanceof short[] shortArray)
					{
						int index = 0;
						for (JsonElement jElement : jArray)
						{
							shortArray[index] = (short)elementParser.Parse(null, jElement, annotation);
							index++;
						}
					}
					else if(array instanceof byte[] byteArray)
					{
						int index = 0;
						for (JsonElement jElement : jArray)
						{
							byteArray[index] = (byte)elementParser.Parse(null, jElement, annotation);
							index++;
						}
					}
					else
					{
						FlansMod.LOGGER.error("Unknown array type " + array.getClass());
					}

					return array;
				}
				return new Object[0];
			}
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as " + elementType + "[] due to exception: " + e); throw e; }
		}
	}

	public static class PolymorphicDeserializer<TClass> implements FieldParseMethod
	{
		private final Class<? extends TClass> baseClass;
		private final HashMap<String, ClassDeserializer<? extends TClass>> implementations;
		private final Optional<ClassDeserializer<? extends TClass>> defaultDeserializer;

		public PolymorphicDeserializer(Class<? extends TClass> cl, ClassDeserializer<? extends TClass> deser)
		{
			baseClass = cl;
			implementations = new HashMap<>();
			defaultDeserializer = Optional.of(deser);
		}

		public PolymorphicDeserializer(Class<? extends TClass> cl)
		{
			baseClass = cl;
			implementations = new HashMap<>();
			defaultDeserializer = Optional.empty();
		}

		public void AddImplementation(String typeName, ClassDeserializer<? extends TClass> implementation)
		{
			implementations.put(typeName, implementation);
		}

		@Override
		public Object Parse(Object ref, JsonElement jNode, JsonField annotation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
		{
			try
			{
				if(jNode instanceof JsonObject jObject)
				{
					if(jObject.has("type"))
					{
						String type = jObject.get("type").getAsString();
						if(implementations.containsKey(type))
						{
							// Note: Can't use an existing ref, as it may be the wrong type
							return implementations.get(type).Parse(null, jNode, annotation);
						}
						else FlansMod.LOGGER.warn("Could not parse polymorphic object as type '" + type + "' is not known.");

					}
					else FlansMod.LOGGER.warn("Polymorphic object lacks a type field.");

					if(defaultDeserializer.isPresent())
					{
						return defaultDeserializer.get().Parse(null, jNode, annotation);
					}
					else FlansMod.LOGGER.warn("Polymorphic object could not resolve type AND had no valid default");
				}
				else FlansMod.LOGGER.warn("Polymorphic object expected a Json Object.");
				return null;
			}
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " as a polymorphism of " + baseClass + " due to exception: " + e); throw e; }
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
			try
			{
				if (ref == null)
					ref = classRef.getDeclaredConstructor().newInstance();

				for (var kvp : jNode.getAsJsonObject().entrySet())
				{

					Field targetField = memberFields.get(kvp.getKey());
					if (targetField != null)
					{
						FieldParseMethod parser = GetParserFor(targetField);
						if (parser != null)
						{
							Object targetObject = targetField.get(ref);
							targetObject = parser.Parse(targetObject, kvp.getValue(), targetField.getAnnotation(JsonField.class));
							targetField.set(ref, targetObject);
						}
					}
				}

				return ref;
			}
			catch(Exception e) { FlansMod.LOGGER.error("Failed to parse JsonNode " + jNode + " into " + ref + " as " + classRef + " due to exception: " + e); throw e; }
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
			FlansMod.LOGGER.error("Failed to load " + definition.Location);
			FlansMod.LOGGER.error(e.getMessage());
			for(StackTraceElement stackTrace : e.getStackTrace())
			{
				FlansMod.LOGGER.error(stackTrace.toString());
			}
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
