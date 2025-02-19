package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Index: Coluna
 * Valor: Linha
 */

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static final Random random = new Random();

    private static final int POPULATION_SIZE = 20;
    private static final double CROSSOVER_RATE = 0.8;
    private static final double MUTATION_RATE = 0.03;
    private static final int MAX_GENERATIONS = 1000;
    private static final int BOARD_SIZE = 8; // Quantidade de rainhas
    private static final int CHROMOSOME_LENGTH = BOARD_SIZE * 3;

    public static void main(String[] args) {
        long inicio = System.nanoTime();
        
        List<String> population = generateInitialPopulation(POPULATION_SIZE); // Gera 8 combinações aleatórias. Ex: 001 - Rainha 1
        int generation = 0;
        
        List<Integer> fitnessScores = new ArrayList<>();

        while (generation < MAX_GENERATIONS) {
            List<String> newPopulation = new ArrayList<>();

            // Avaliação da população
            fitnessScores = evaluatePopulation(population);

            // Verifica se a solução ótima foi encontrada
            if (fitnessScores.contains(0)) {
                log.log(Level.INFO, "Solução encontrada na geração {0}", generation);

                int solutionIndex = fitnessScores.indexOf(0);

                log.log(Level.INFO, "Solução: " + population.get(solutionIndex));

                break;
            }

            // Seleção dos pais e geração de nova população
            while (newPopulation.size() < POPULATION_SIZE) {
                String parent1 = selectParent(population, fitnessScores);
                String parent2 = selectParent(population, fitnessScores);

                String offspring1 = parent1;
                String offspring2 = parent2;
                
                // Crossover
                if (Math.random() < CROSSOVER_RATE) {
                    String[] offspring = crossover(parent1, parent2);
                    offspring1 = offspring[0];
                    offspring2 = offspring[1];
                }

                // Mutação
                offspring1 = mutate(offspring1);
                offspring2 = mutate(offspring2);

                newPopulation.add(offspring1);
                newPopulation.add(offspring2);
            }

            // Seleção de sobreviventes: elitista
            population = elitistSelection(population, newPopulation);

            generation++;
        }

        if (generation == MAX_GENERATIONS) log.info("Número máximo de gerações alcançado sem encontrar a solução.");
        
        long fim = System.nanoTime();
        
        long duracao = (fim - inicio);
        double duracao_MicroSec = duracao / 1000;

        // Sessão de Teste
        int soma = 0;
        for(Integer value : fitnessScores) {
            soma += value;
        }
        System.out.println("Nº de colisões: " + soma);
        System.out.println("Tempo em microsegundo(s): " + duracao_MicroSec);
    }

    private static List<String> generateInitialPopulation(int size) {
        List<String> population = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            StringBuilder chromosome = new StringBuilder();

            for (int j = 0; j < CHROMOSOME_LENGTH; j++) chromosome.append(random.nextInt(2));
        
            population.add(chromosome.toString());
        }

        return population;
    }

    private static List<Integer> evaluatePopulation(List<String> population) {
        List<Integer> fitnessScores = new ArrayList<>();
        
        for (String individual : population) fitnessScores.add(fitness(individual));
        
        return fitnessScores;
    }

    private static int fitness(String individual) {
        int clashes = 0;
        int[] columns = new int[BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            String columnBits = individual.substring(i * 3, (i + 1) * 3); //Pega a posição de uma rainha.
            columns[i] = Integer.parseInt(columnBits, 2);
        }

        for (int i = 0; i < BOARD_SIZE; i++) 
            for (int j = i + 1; j < BOARD_SIZE; j++) 
                if (columns[i] == columns[j] || Math.abs(columns[i] - columns[j]) == j - i) 
                    clashes++;

        return clashes;
    }

    private static String selectParent(List<String> population, List<Integer> fitnessScores) {
        Double totalFitness = fitnessScores.stream().mapToDouble(score -> (double) 1 / (double)(1 + score)).sum();
        int randomValue = random.nextInt(totalFitness.intValue());
        int currentSum = 0;

        for (int i = 0; i < population.size(); i++) {
            currentSum += 1 / (1 + fitnessScores.get(i));

            if (currentSum > randomValue) return population.get(i);
        }

        return population.get(population.size() - 1);
    }

    private static String[] crossover(String parent1, String parent2) {
        int crossoverPoint = random.nextInt(CHROMOSOME_LENGTH);

        String offspring1 = parent1.substring(0, crossoverPoint) + parent2.substring(crossoverPoint);
        String offspring2 = parent2.substring(0, crossoverPoint) + parent1.substring(crossoverPoint);

        return new String[]{offspring1, offspring2};
    }

    private static String mutate(String individual) {
        StringBuilder mutated = new StringBuilder(individual);

        for (int i = 0; i < CHROMOSOME_LENGTH; i++) 
            if (random.nextDouble() < MUTATION_RATE) 
                mutated.setCharAt(i, mutated.charAt(i) == '0' ? '1' : '0');

        return mutated.toString();
    }

    private static List<String> elitistSelection(List<String> population, List<String> newPopulation) {
        List<String> combinedPopulation = new ArrayList<>(population);

        combinedPopulation.addAll(newPopulation);

        combinedPopulation.sort((ind1, ind2) -> fitness(ind1) - fitness(ind2));

        return new ArrayList<>(combinedPopulation.subList(0, POPULATION_SIZE));
    }
}
