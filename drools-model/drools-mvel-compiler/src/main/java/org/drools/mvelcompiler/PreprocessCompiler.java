/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.mvelcompiler;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import org.drools.mvel.parser.MvelParser;
import org.drools.mvel.parser.ast.expr.ModifyStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.javaparser.ast.NodeList.nodeList;

// A special case of compiler in which
// * the modify statements are processed
// * multi line text blocks are converted to Strings
public class PreprocessCompiler {
    private static final Logger logger          = LoggerFactory.getLogger(PreprocessCompiler.class);

    private static final PreprocessPhase preprocessPhase = new PreprocessPhase();

    public CompiledBlockResult compile(String mvelBlock) {

        BlockStmt mvelExpression = MvelParser.parseBlock(mvelBlock);

        preprocessPhase.removeEmptyStmt(mvelExpression);

        mvelExpression.findAll(TextBlockLiteralExpr.class).forEach(e -> {
            Optional<Node> parentNode = e.getParentNode();

            StringLiteralExpr stringLiteralExpr = preprocessPhase.replaceTextBlockWithConcatenatedStrings(e);

            parentNode.ifPresent(p -> {
                if(p instanceof VariableDeclarator) {
                    ((VariableDeclarator) p).setInitializer(stringLiteralExpr);
                } else if(p instanceof MethodCallExpr) {
                    // """exampleString""".formatted("arg0", 2);
                    ((MethodCallExpr) p).setScope(stringLiteralExpr);
                }
            });
        });

        Set<String> usedBindings = new HashSet<>();
        mvelExpression.findAll(ModifyStatement.class)
                .forEach(s -> {
                    Optional<Node> parentNode = s.getParentNode();
                    PreprocessPhase.PreprocessPhaseResult invoke = preprocessPhase.invoke(s);
                    usedBindings.addAll(invoke.getUsedBindings());
                    parentNode.ifPresent(p -> {
                        if (p instanceof BlockStmt){
                            BlockStmt parentBlock = (BlockStmt) p;
                            if(invoke.getUsedBindings().isEmpty()){
                                int lastBlockIdx = parentBlock.getStatements().size();
                                if (parentBlock.getStatements().getLast().get() instanceof BreakStmt){
                                    lastBlockIdx = lastBlockIdx - 1;
                                }
                                parentBlock.addStatement(lastBlockIdx, new MethodCallExpr(null, "update", nodeList(s.getModifyObject().asNameExpr())));
                            } else {
                                for (String modifiedFact : invoke.getUsedBindings()) {
                                    int lastBlockIdx = parentBlock.getStatements().size();
                                    if (parentBlock.getStatements().getLast().get() instanceof BreakStmt){
                                        lastBlockIdx = lastBlockIdx - 1;
                                    }
                                    parentBlock.addStatement(lastBlockIdx, new MethodCallExpr(null, "update", nodeList(new NameExpr(modifiedFact))));
                                }
                            }
                        } else if (p instanceof IfStmt) {
                            Optional<Node> blockNode = p.getChildNodes().stream().filter(node -> node instanceof BlockStmt).findFirst();
                            blockNode.ifPresent(ifBlock -> {
                                if (ifBlock instanceof BlockStmt){
                                    BlockStmt internalBlock = (BlockStmt) ifBlock;
                                    for (String modifiedFact : invoke.getUsedBindings()) {
                                        internalBlock.addStatement(new MethodCallExpr(null, "update", nodeList(new NameExpr(modifiedFact))));
                                    }
                                } else {
                                    logger.warn("Found modify statement with incompatible type "+ p.getClass()+" inside if" +": "+mvelBlock);
                                }
                            });
                        } else {
                            logger.warn("Found modify statement with incompatible type "+ p.getClass()+": "+mvelBlock);
                        }
                    });
                    s.remove();
                });

        return new CompiledBlockResult(mvelExpression.getStatements()).setUsedBindings(usedBindings);
    }
}
