package com.restphone.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite

//@RunWith( classOf[ JUnitRunner ] )
class JavaSignatureParserTest extends FunSuite {
  val p = new JavaSignatureParser

  test( "can parse a Java primitive" ) {
    expect( "boolean" ) {
      p.parseAll( p.typeSignature, "Z" ).get.toJava
    }

  }

  test( "java.util.List<E>" ) {
    expect( "java.util.List<E>" ) {
      p.parseAll( p.typeSignature, "Ljava/util/List<TE;>;" ).get.toJava
    }

  }

  test( "Number" ) {
    expect( "Number" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/lang/Number;" )
      result.get.toJava
    }
  }

  test( "List<Number>" ) {
    expect( "java.util.List<Number>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/List<Ljava/lang/Number;>;" )
      result.get.toJava
    }
  }

  test( "java.util.List<? extends Number>" ) {
    expect( "java.util.List<? extends Number>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/List<+Ljava/lang/Number;>;" )
      println( result )
      println( result.get.typesUsed )
      result.get.toJava
    }
  }

  test( "java.util.List<? super Number>" ) {
    expect( "java.util.List<? super Number>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/List<-Ljava/lang/Number;>;" )
      result.get.toJava
    }
  }

  test( "List<List<String>[]>" ) {
    expect( "java.util.List<java.util.List<String>[]>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/List<[Ljava/util/List<Ljava/lang/String;>;>;" )
      result.get.toJava
    }
  }

  test( "HashMap<K, V>.HashIterator<K>" ) {
    expect( "java.util.HashMap<K, V>.HashIterator<K, V>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/HashMap<TK;TV;>.HashIterator<TK;TV;>;" )
      result.get.toJava
    }
  }

  test( "java.util.List<?>" ) {
    expect( "java.util.List<?>" ) {
      val result = p.parseAll( p.typeSignature, "Ljava/util/List<*>;" )
      result.get.toJava
    }
  }

  test( "(II)V" ) {
    expect( "void methodName ( int , int )" ) {
      JavaSignatureParser.parseMethod("(II)V").get.toJava
    }
  }
}
