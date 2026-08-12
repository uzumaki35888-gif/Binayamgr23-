package com.example.data

data class FormulaItem(
    val title: String,
    val formula: String,
    val description: String,
    val variables: String,
    val category: String,
    val exampleQuestion: String
)

object FormulaRepository {
    val formulas = listOf(
        // Algebra
        FormulaItem(
            title = "Quadratic Formula",
            formula = "x = (-b ± √(b² - 4ac)) / (2a)",
            description = "Finds the roots of a quadratic equation ax² + bx + c = 0",
            variables = "a = coef of x², b = coef of x, c = constant",
            category = "Algebra",
            exampleQuestion = "Solve x² - 5x + 6 = 0 using the quadratic formula"
        ),
        FormulaItem(
            title = "Slope-Intercept Form",
            formula = "y = mx + b",
            description = "Equation of a straight line with slope m and y-intercept b",
            variables = "m = slope, b = y-intercept",
            category = "Algebra",
            exampleQuestion = "Find the equation of line passing through (0, 3) with slope 2"
        ),
        FormulaItem(
            title = "Logarithm Change of Base",
            formula = "log_b(x) = ln(x) / ln(b)",
            description = "Converts logarithms from base b to natural log or base 10",
            variables = "b = original base, x = argument",
            category = "Algebra",
            exampleQuestion = "Evaluate log_2(32)"
        ),
        FormulaItem(
            title = "Sum of Arithmetic Series",
            formula = "S_n = (n / 2) * (2a + (n - 1)d)",
            description = "Calculates the sum of first n terms in an arithmetic sequence",
            variables = "n = number of terms, a = first term, d = common difference",
            category = "Algebra",
            exampleQuestion = "Find the sum of first 20 terms of sequence 3, 7, 11, 15..."
        ),

        // Geometry
        FormulaItem(
            title = "Pythagorean Theorem",
            formula = "a² + b² = c²",
            description = "Relates sides of a right triangle where c is hypotenuse",
            variables = "a, b = legs, c = hypotenuse",
            category = "Geometry",
            exampleQuestion = "Find hypotenuse if legs are 6 cm and 8 cm"
        ),
        FormulaItem(
            title = "Area of a Circle",
            formula = "A = π * r²",
            description = "Calculates total area enclosed inside a circle",
            variables = "r = radius, π ≈ 3.14159",
            category = "Geometry",
            exampleQuestion = "Calculate area of circle with radius r = 7 cm"
        ),
        FormulaItem(
            title = "Volume of a Sphere",
            formula = "V = (4 / 3) * π * r³",
            description = "Finds the 3D space occupied by a sphere",
            variables = "r = radius of sphere",
            category = "Geometry",
            exampleQuestion = "Find volume of sphere with radius 5 cm"
        ),

        // Trigonometry
        FormulaItem(
            title = "Trigonometric Identity",
            formula = "sin²(θ) + cos²(θ) = 1",
            description = "Fundamental Pythagorean identity in trigonometry",
            variables = "θ = angle in degrees or radians",
            category = "Trigonometry",
            exampleQuestion = "If sin(θ) = 3/5, find cos(θ)"
        ),
        FormulaItem(
            title = "Law of Sines",
            formula = "(a / sin A) = (b / sin B) = (c / sin C)",
            description = "Relates side lengths to angles for any triangle",
            variables = "a,b,c = side lengths, A,B,C = opposite angles",
            category = "Trigonometry",
            exampleQuestion = "Find side b in triangle with a = 10, A = 30°, B = 45°"
        ),
        FormulaItem(
            title = "Law of Cosines",
            formula = "c² = a² + b² - 2ab * cos(C)",
            description = "Generalization of Pythagorean theorem for all triangles",
            variables = "a,b,c = side lengths, C = angle opposite to c",
            category = "Trigonometry",
            exampleQuestion = "Find c when a = 5, b = 7, angle C = 60°"
        ),

        // Calculus
        FormulaItem(
            title = "Power Rule for Derivatives",
            formula = "d/dx (x^n) = n * x^(n-1)",
            description = "Calculates derivative of a polynomial term",
            variables = "n = exponent power",
            category = "Calculus",
            exampleQuestion = "Find derivative of f(x) = 4x³ - 2x² + 5"
        ),
        FormulaItem(
            title = "Power Rule for Integrals",
            formula = "∫ x^n dx = (x^(n+1)) / (n + 1) + C",
            description = "Calculates indefinite integral for x^n (n ≠ -1)",
            variables = "n = power, C = constant of integration",
            category = "Calculus",
            exampleQuestion = "Integrate ∫ (3x² + 2x) dx"
        ),
        FormulaItem(
            title = "Product Rule",
            formula = "d/dx [u * v] = u'v + uv'",
            description = "Derivative of the product of two functions",
            variables = "u, v = differentiable functions of x",
            category = "Calculus",
            exampleQuestion = "Find derivative of f(x) = x² * sin(x)"
        ),

        // Statistics
        FormulaItem(
            title = "Standard Deviation",
            formula = "σ = √[ Σ(x_i - μ)² / N ]",
            description = "Measures the amount of variation or dispersion in data set",
            variables = "x_i = values, μ = mean, N = number of values",
            category = "Statistics",
            exampleQuestion = "Calculate standard deviation for set {2, 4, 4, 4, 5, 5, 7, 9}"
        )
    )
}
