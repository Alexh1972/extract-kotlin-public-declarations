package org.declarations

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierType

/**
 * A utility class for analyzing and processing Kotlin PSI elements.
 */
class KtElementAnalyser {
    companion object {
        /**
         * Processes a [KtClass] and returns its string representation.
         *
         * @param ktClass The Kotlin class to process.
         * @param end A string to append at the end.
         * @param ident The indentation level.
         * @return The formatted string representation of the class.
         */
        fun process(ktClass: KtClass, end: String = "", ident: Int = 0): String {
            if (!isPublic(ktClass))
                return ""

            val strBuilder = StringBuilder()
            strBuilder.append(KtUtils.getIdent(ident))
                .append(if (ktClass.isEnum()) "enum " else "")
                .append(if (ktClass.isAbstract()) "abstract " else "")
                .append(ktClass.getClassOrInterfaceKeyword()?.text)
                .append(" ")
                .append(ktClass.name)
                .append(ktClass.primaryConstructor?.text ?: "")
                .append(
                    if (ktClass.superTypeListEntries.isNotEmpty())
                        " : " + ktClass.superTypeListEntries
                            .stream()
                            .map { e -> e.text }
                            .toList()
                            .joinToString(", ")
                    else ""
                )
                .append(" {\n")

            ktClass.companionObjects.forEach { decl ->
                strBuilder.append(process(decl, ident + 1, "\n"))
            }

            ktClass.children.forEach { decl ->
                when (decl) {
                    is KtClassBody -> strBuilder.append(process(decl, ident = ident + 1))
                    is KtClass -> strBuilder.append(process(decl, ident = ident + 1, end = "\n"))
                }
            }

            strBuilder.append(KtUtils.getIdent(ident))
            strBuilder.append("}")
            strBuilder.append(end)

            return strBuilder.toString()
        }

        /**
         * Processes a [KtObjectDeclaration] (companion object) and returns its string representation.
         *
         * @param ktObjectDeclaration The object declaration to process.
         * @param ident The indentation level.
         * @return The formatted string representation of the object.
         */
        fun process(ktObjectDeclaration: KtObjectDeclaration, ident: Int = 0, end: String = ""): String {
            if (!isPublic(ktObjectDeclaration))
                return ""

            val strBuilder = StringBuilder()

            strBuilder.append(KtUtils.getIdent(ident))
                .append("companion object ")
                .append(
                    if (ktObjectDeclaration.nameIdentifier != null)
                        ktObjectDeclaration.nameIdentifier?.text + " "
                    else ""
                )
                .append("{\n")

            ktObjectDeclaration.children.forEach { decl ->
                when (decl) {
                    is KtClassBody -> strBuilder.append(process(decl, ident = ident + 1))
                }
            }

            strBuilder.append(KtUtils.getIdent(ident))
                .append("}")
                .append(end)

            return strBuilder.toString()
        }

        /**
         * Processes a [KtClassBody] and returns its string representation.
         *
         * @param ktClassBody The class body to process.
         * @param ident The indentation level.
         * @return The formatted string representation of the class body.
         */
        fun process(ktClassBody: KtClassBody, ident: Int = 0): String {
            val strBuilder = StringBuilder()
            ktClassBody.children.forEach { decl ->
                when (decl) {
                    is KtNamedFunction -> strBuilder.append(process(decl, ident = ident, end = "\n"))
                    is KtProperty -> strBuilder.append(process(decl, ident = ident, end = "\n"))
                    is KtEnumEntry -> strBuilder.append(process(decl, ident = ident, end = "\n"))
                    is KtClass -> strBuilder.append(process(decl, ident = ident, end = "\n"))
                }
            }

            return strBuilder.toString()
        }

        /**
         * Processes a [KtEnumEntry] and returns its string representation.
         *
         * @param enumEntry The enum entry to process.
         * @param ident The indentation level.
         * @return The formatted string representation of the enum entry.
         */
        fun process(enumEntry: KtEnumEntry, ident: Int = 0): String {
            return StringBuilder().append(KtUtils.getIdent(ident)).append(enumEntry.text).toString()
        }

        /**
         * Processes a [KtFunction] and returns its string representation.
         *
         * @param ktFunction The function to process.
         * @param end A string to append at the end.
         * @param ident The indentation level.
         * @return The formatted string representation of the function.
         */
        fun process(ktFunction: KtFunction, end: String = "", ident: Int = 0): String {
            if (!isPublic(ktFunction))
                return ""

            val strBuilder = StringBuilder()

            strBuilder.append(KtUtils.getIdent(ident))
                .append(if (ktFunction.hasModifier(KtTokens.OVERRIDE_KEYWORD)) "override " else "")
                .append("fun ")
                .append(ktFunction.name)
                .append("(")
                .append(ktFunction.valueParameters.stream()
                    .map { k -> k.text }
                    .toList()
                    .joinToString(", "))
                .append(")")
                .append(if (ktFunction.hasDeclaredReturnType()) ": " + ktFunction.typeReference!!.text else "")
                .append(
                    if (ktFunction.bodyExpression is KtBlockExpression) ""
                    else " = " + ktFunction.bodyExpression?.text
                )
                .append(end)

            return strBuilder.toString()
        }

        /**
         * Processes a [KtProperty] and returns its string representation.
         *
         * @param ktProperty The property to process.
         * @param end A string to append at the end.
         * @param ident The indentation level.
         * @return The formatted string representation of the property.
         */
        fun process(ktProperty: KtProperty, end: String = "", ident: Int = 0): String {
            if (!isPublic(ktProperty))
                return ""

            return StringBuilder().append(KtUtils.getIdent(ident))
                .append(ktProperty.text)
                .append(end)
                .toString()
        }

        /**
         * Checks if a [KtNamedDeclaration] is public.
         *
         * @param ktElement The element to check.
         * @return `true` if the element is public, `false` otherwise.
         */
        private fun isPublic(ktElement: KtNamedDeclaration): Boolean {
            return ktElement.visibilityModifierType() == null
                    || ktElement.visibilityModifierType()!!.value == "public"
        }
    }
}