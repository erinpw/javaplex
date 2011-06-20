function [cycle_sum, homotopies] = hom_parameterization(domain_stream, codomain_stream)
% [cycle_sum, homotopies] = hom_parameterization(domain_stream, codomain_stream)
%
% INPUTS
% domain_stream: an object of type AbstractFilteredStream<Simplex> which
% contains the contents of the domain
% codomain_stream: an object of type AbstractFilteredStream<Simplex> which
% contains the contents of the codomain
%
% OUTPUTS
% cycle_sum: a chain map generated by representatives of significant
% intervals in the homology of the hom-complex (a sparse matrix)
% homotopies: a cell array containing the set of chain homotopies as sparse
% matrices

    import edu.stanford.math.plex4.*;
    import edu.stanford.math.primitivelib.collections.utility.*;

    matrix_converter = api.Plex4.createHomMatrixConverter(domain_stream, codomain_stream);
    
    hom_stream = api.Plex4.createHomStream(domain_stream, codomain_stream);
    hom_stream.finalizeStream();

    persistence = api.Plex4.getRationalHomAlgorithm();

    chain_module = persistence.getChainModule();

    homotopies = hom_stream.getHomotopiesAsDouble(chain_module);

    homotopy_matrices = cell(homotopies.size(), 1);

    m = codomain_stream.getSize();
    n = domain_stream.getSize();

    index = 1;
    iterator = homotopies.iterator();
    while (iterator.hasNext())
        homotopy = iterator.next();
        homotopy_matrices{index} = to_sparse_matlab_matrix(homotopy, matrix_converter);
        index = index + 1;
    end

    barcode_collection = persistence.computeAnnotatedIntervals(hom_stream);

    intervals = barcode_collection.getBarcode(0).getIntervals();

    cycle_sum = chain_module.createNewSum();

    iterator = intervals.iterator();
    cycle_index = 1;
    while (iterator.hasNext())
        interval_generator_pair = iterator.next();
        interval = interval_generator_pair.getFirst();
        generator = interval_generator_pair.getSecond();
        if (interval.isInfinite())% && (cycle_index == 1))
            chain_module.accumulate(cycle_sum, generator);
        end
        cycle_index = cycle_index + 1;
    end
    
    cycle_sum = hom_stream.toDoubleFormalSum(cycle_sum);
    cycle_sum = to_sparse_matlab_matrix(cycle_sum, matrix_converter);
    homotopies = homotopy_matrices;
end