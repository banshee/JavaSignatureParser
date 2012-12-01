package com.restphone.jartender

import scalaz._
import scalaz.Scalaz._
import scala.util.parsing.combinator._
import scala.Option.option2Iterable

// This parser uses the grammar from section 4.1.1 of the 
// ASM 4.0: A Java bytecode engineering library
// documentation.
// 
// Here's the gist of it:
// TypeSignature: Z | C | B | S | I | F | J | D | FieldTypeSignature
// FieldTypeSignature: ClassTypeSignature | [ TypeSignature | TypeVar
// ClassTypeSignature: L Id ( / Id )* TypeArgs? ( . Id TypeArgs? )* ;
// TypeArgs: < TypeArg+ >
// TypeArg: * | ( + | - )? FieldTypeSignature
// TypeVar: T Id ;
//
// Note that there's still some work to be done - this doesn't distinguish between type, method, and 
// class signatures.

class JavaSignatureParser extends JavaTokenParsers {
  // Each of the lines of the grammar gets turned into a case class.  There are also methods
  // with the same name (starting with a lower-case letter) that implement the parser for the
  // grammar element.

  def methodSignature : Parser[ MethodSignature ] = {
    val argumentSignature = "(" ~> rep( typeSignature ) <~ ")"
    val result = ( argumentSignature ~ typeSignature ) ^^ { case a ~ b => MethodSignature( a, b ) }
    result
  }

  // This is the heart of the parser.  Take each line of the grammer and turn it into
  // a Parser[X].

  def typeSignature : Parser[ TypeSignature ] = {
    // I use TypeDescriptor to mean all the single-character elements for Java primitives
    sealed abstract class TypeDescriptor extends TypeSignatureElement {
      override def typesUsed = Set.empty // never care about primitives
    }
    case object TypeDescriptorZ extends TypeDescriptor {
      override def toJava = "boolean"
    }
    case object TypeDescriptorC extends TypeDescriptor {
      override def toJava = "char"
    }
    case object TypeDescriptorB extends TypeDescriptor {
      override def toJava = "byte"
    }
    case object TypeDescriptorS extends TypeDescriptor {
      override def toJava = "short"
    }
    case object TypeDescriptorI extends TypeDescriptor {
      override def toJava = "int"
    }
    case object TypeDescriptorF extends TypeDescriptor {
      override def toJava = "float"
    }
    case object TypeDescriptorJ extends TypeDescriptor {
      override def toJava = "long"
    }
    case object TypeDescriptorD extends TypeDescriptor {
      override def toJava = "double"
    }
    case object TypeDescriptorV extends TypeDescriptor {
      override def toJava = "void"
    }

    def typeDescriptor : Parser[ TypeDescriptor ] = {
      def typeDescriptorZ : Parser[ TypeDescriptor ] = "Z" ^^ { _ => TypeDescriptorZ }
      def typeDescriptorC : Parser[ TypeDescriptor ] = "C" ^^ { _ => TypeDescriptorC }
      def typeDescriptorB : Parser[ TypeDescriptor ] = "B" ^^ { _ => TypeDescriptorB }
      def typeDescriptorS : Parser[ TypeDescriptor ] = "S" ^^ { _ => TypeDescriptorS }
      def typeDescriptorI : Parser[ TypeDescriptor ] = "I" ^^ { _ => TypeDescriptorI }
      def typeDescriptorF : Parser[ TypeDescriptor ] = "F" ^^ { _ => TypeDescriptorF }
      def typeDescriptorJ : Parser[ TypeDescriptor ] = "J" ^^ { _ => TypeDescriptorJ }
      def typeDescriptorD : Parser[ TypeDescriptor ] = "D" ^^ { _ => TypeDescriptorD }
      def typeDescriptorV : Parser[ TypeDescriptor ] = "V" ^^ { _ => TypeDescriptorV }
      ( typeDescriptorZ |
        typeDescriptorC |
        typeDescriptorB |
        typeDescriptorS |
        typeDescriptorI |
        typeDescriptorF |
        typeDescriptorJ |
        typeDescriptorD |
        typeDescriptorV )
    }
    ( typeDescriptor | fieldTypeSignature ) ^^ TypeSignature
  }

  def fieldTypeSignature : Parser[ FieldTypeSignature ] = {
    case class ArrayTypeSignature( xs : TypeSignature ) extends FieldTypeSignatureElement {
      override def toJava = xs.toJava + "[]"
      override def typesUsed = xs.typesUsed
    }

    def arrayTypeSignature : Parser[ ArrayTypeSignature ] = "[" ~> typeSignature ^^ { ArrayTypeSignature( _ ) }

    ( classTypeSignature | arrayTypeSignature | typeVar ) ^^ FieldTypeSignature
  }

  def classTypeSignature : Parser[ ClassTypeSignature ] = {
    val nestedClasses = "." ~> javaName ~ opt( typeArgs ) ^^ { case a ~ b => NestedClass( a, b ) }

    ( "L" ~> rep1sep( javaName, "/" ) ~ opt( typeArgs ) ~ rep( nestedClasses ) <~ ";" ) ^^
      { case ids ~ optionalTypeArgs ~ extensionElements => ClassTypeSignature( JavaName( ids.map( _.s ).mkString( "." ) ), optionalTypeArgs, extensionElements ) }
  }

  def typeArgs : Parser[ TypeArgs ] = "<" ~> rep( typeArg ) <~ ">" ^^ TypeArgs

  def typeArg : Parser[ TypeArg ] = {
    // Signatures use signed FieldTypeSignatures for things like
    // java.util.List<? super Number> -- Ljava/util/List<-Ljava/lang/Number;>
    // and * (star) for 
    // java.util.List<?> -- Ljava/util/List<*>;

    case class FieldTypeSignatureWithSign( sign : SignForFieldTypeSignature, fts : FieldTypeSignature ) extends HasToJavaMethod with TypeArgElement {
      override def toJava = sign.toJava + fts.toJava
      override def typesUsed = fts.typesUsed
    }

    sealed abstract class SignForFieldTypeSignature extends HasToJavaMethod
    case object Plus extends SignForFieldTypeSignature {
      override def toJava = "? extends "
    }
    case object Minus extends SignForFieldTypeSignature {
      override def toJava = "? super "
    }

    case object Star extends TypeArgElement {
      override def toJava = "?"
      override def typesUsed = Set.empty
    }

    def typeStar : Parser[ TypeArgElement ] = "*" ^^ { _ => Star }

    def fieldTypeSignatureWithSign : Parser[ FieldTypeSignatureWithSign ] = {
      def typePlusOrMinus : Parser[ SignForFieldTypeSignature ] = {
        def plus = "+" ^^ { _ => Plus }
        def minus = "-" ^^ { _ => Minus }
        plus | minus
      }
      typePlusOrMinus ~ fieldTypeSignature ^^
        { case sign ~ fts => FieldTypeSignatureWithSign( fts = fts, sign = sign ) }
    }
    ( typeStar | fieldTypeSignatureWithSign | fieldTypeSignature ) ^^ { x => TypeArg( x ) }
  }

  def typeVar : Parser[ TypeVar ] = "T" ~> javaName <~ ";" ^^ TypeVar

  def javaName : Parser[ JavaName ] = ident ^^ JavaName
}

case class MethodSignature( paramSignature : List[ TypeSignature ], resultSignature : TypeSignature ) extends HasTypesUsedMethod {
  lazy val paramSignatureTypes = paramSignature flatMap { _.typesUsed }
  lazy val typesUsed = ( paramSignatureTypes ++ resultSignature.typesUsed ) toSet
  lazy val toJava = {
    var a = List( resultSignature.toJava, "methodName", "(" )
    var b = JavaSignatureParser.interpolate( paramSignature map { _.toJava }, "," )
    var c = List( ")" )
    ( a ++ b ++ c ) mkString " "
  }
}

case class TypeSignature( x : TypeSignatureElement ) extends HasToJavaMethod with HasTypesUsedMethod {
  override def toJava = x.toJava
  override def typesUsed = x.typesUsed
}

case class FieldTypeSignature( x : FieldTypeSignatureElement ) extends TypeSignatureElement with TypeArgElement {
  override def toJava = x.toJava
  override def typesUsed = x.typesUsed
}

case class ClassTypeSignature( ids : JavaName, optionalTypeArgs : Option[ TypeArgs ], extension : List[ NestedClass ] ) extends FieldTypeSignatureElement {
  override def toJava = ids.toJava + optionalTypeArgs.map( _.toJava ).mkString + ( extension map { _.toJava } mkString ( "." ) )
  override def typesUsed = {
    val a = optionalTypeArgs.map { _.typesUsed } getOrElse List.empty
    val b = extension flatMap { _.typesUsed }
    ids.typesUsed ++ a ++ b
  }
}

// NestedClass isn't one of the top level elements, but it makes it easier to understand the 
// code that parses the ( . Id TypeArgs? )* part of a ClassTypeSignature
case class NestedClass( javaName : JavaName, typeArgs : Option[ TypeArgs ] ) extends HasToJavaMethod with HasTypesUsedMethod {
  override def toJava = "." + javaName.toJava + typeArgs.map { _.toJava }.mkString
  val elements = ( typeArgs map { _.typesUsed } getOrElse List.empty )
  override def typesUsed = javaName.typesUsed ++ ( typeArgs map { _.typesUsed } getOrElse List.empty )
}

case class TypeArgs( typeArgs : List[ TypeArg ] ) extends HasToJavaMethod with HasTypesUsedMethod {
  override def toJava = typeArgs.map( _.toJava ).mkString( "<", ", ", ">" )
  override def typesUsed = typeArgs flatMap { _.typesUsed } toSet
}

case class TypeArg( t : TypeArgElement ) extends HasToJavaMethod with HasTypesUsedMethod {
  override def toJava = t.toJava
  override def typesUsed = t.typesUsed
}

case class TypeVar( t : JavaName ) extends FieldTypeSignatureElement {
  override def toJava = t.toJava
  override def typesUsed = t.typesUsed
}

// It's been a long time since CS 101, so I forget exactly what the bits of a grammar
// are called.  These are the things on the right-hand side.  For example, the first
// line of the grammar is TypeSignature, and it consists of any of the elements for a java
// primitive (Z, C, B etc) or a FieldTypeSignature.  So I mark all of the case classes
// for the primitives and for FieldTypeSignature with TypeSignatureElement.

trait TypeSignatureElement extends HasToJavaMethod with HasTypesUsedMethod
trait FieldTypeSignatureElement extends HasToJavaMethod with HasTypesUsedMethod
trait TypeArgElement extends HasToJavaMethod with HasTypesUsedMethod

// Any element that can be converted to Java code implements this trait.  It's used to
// turn the parsed signature back into Java.
trait HasToJavaMethod {
  def toJava : String
}

trait HasTypesUsedMethod {
  def typesUsed : Set[ JavaName ]
}

// JavaIdentifier is already taken, so I'm using JavaName instead.  (There's probably a way
// to use JavaIdentifier from the parser, but I'll leave that as an exercise for the reader.)
case class JavaName( s : String ) extends HasToJavaMethod with HasTypesUsedMethod {
  override val toJava = s
  override def typesUsed = Set( this )
}

object JavaSignatureParser {
  def parse( s : String ) = {
    val p = new JavaSignatureParser
    p.parseAll( p.typeSignature, s )
  }

  def parseMethod( s : String ) = {
    val p = new JavaSignatureParser
    p.parseAll( p.methodSignature, s )
  }

  def interpolate[ T ]( xs : Iterable[ T ], sep : T ) = ( xs zip Stream.continually( sep ) ).foldLeft( List.empty[ T ] ) { case ( acc, ( a, b ) ) => b :: a :: acc }.reverse.dropRight( 1 )
}
