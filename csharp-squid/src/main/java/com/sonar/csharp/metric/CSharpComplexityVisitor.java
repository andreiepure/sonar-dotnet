/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.csharp.metric;

import com.sonar.csharp.api.CSharpGrammar;
import com.sonar.csharp.api.CSharpKeyword;
import com.sonar.csharp.api.CSharpPunctuator;
import com.sonar.csharp.api.ast.CSharpAstVisitor;
import com.sonar.csharp.api.metric.CSharpMetric;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

/**
 * Visitor that computes the McCabe complexity.
 */
public class CSharpComplexityVisitor extends CSharpAstVisitor {

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() {
    CSharpGrammar g = getCSharpGrammar();
    subscribeTo(g.ifStatement, g.switchStatement, g.labeledStatement, g.whileStatement, g.doStatement, g.forStatement, g.returnStatement,
        g.methodDeclaration, g.getAccessorDeclaration, g.setAccessorDeclaration, g.addAccessorDeclaration, g.removeAccessorDeclaration,
        g.operatorDeclaration, g.constructorDeclaration, g.destructorDeclaration, g.staticConstructorDeclaration, CSharpPunctuator.AND_OP,
        CSharpPunctuator.OR_OP, CSharpKeyword.CASE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitNode(AstNode node) {
    if (node.is(getCSharpGrammar().returnStatement) && isLastReturnStatement(node)) {
      return;
    }
    peekPhysicalSourceCode().add(CSharpMetric.COMPLEXITY, 1);
  }

  private boolean isLastReturnStatement(AstNode node) {
    AstNode currentNode = node;
    AstNode parent = currentNode.getParent();
    AstNodeType blockType = getCSharpGrammar().block;
    while ( !parent.getType().equals(blockType)) {
      currentNode = parent;
      parent = currentNode.getParent();
    }
    // here, parent is a block
    if ( !currentNode.nextSibling().is(CSharpPunctuator.RCURLYBRACE)) {
      return false;
    }
    AstNodeType parentType = parent.getParent().getType();
    if (parentType.equals(getCSharpGrammar().methodBody) || parentType.equals(getCSharpGrammar().accessorBody)
        || parentType.equals(getCSharpGrammar().addAccessorDeclaration) || parentType.equals(getCSharpGrammar().removeAccessorDeclaration)
        || parentType.equals(getCSharpGrammar().operatorBody) || parentType.equals(getCSharpGrammar().constructorBody)
        || parentType.equals(getCSharpGrammar().destructorBody) || parentType.equals(getCSharpGrammar().staticConstructorBody)) {
      return true;
    }
    return false;
  }
}
