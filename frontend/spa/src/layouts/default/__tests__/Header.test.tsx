import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, expect, test } from 'vitest';

import Header from '../Header';

describe('Header', () => {
    test('renders the header with menu items', () => {
        render(
            <BrowserRouter>
                <Header />
            </BrowserRouter>
        );

        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('Demo')).toBeInTheDocument();
        expect(screen.getByText('header.login')).toBeInTheDocument();
        expect(screen.getByText('header.register')).toBeInTheDocument();
    });
});
