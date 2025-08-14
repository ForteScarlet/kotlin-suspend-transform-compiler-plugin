import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';
import Translate from '@docusaurus/Translate';
import {useColorMode} from '@docusaurus/theme-common';
import dayjs from 'dayjs';
import dayOfYear from 'dayjs/plugin/dayOfYear';

import styles from './index.module.css';

// Initialize dayjs plugin
dayjs.extend(dayOfYear);

// Hash-based gradient selection constants
const LIGHT_GRADIENTS_COUNT = 28;
const DARK_GRADIENTS_COUNT = 15;

// Get day of year from date using dayjs
function getDayOfYear(date?: Date): number {
    return dayjs(date).dayOfYear();
}

// Select gradient class based on theme and day of year
function selectGradientClass(isDarkTheme: boolean): string {
    const dayOfYear = getDayOfYear(); // Uses current date by default

    if (isDarkTheme) {
        const index = dayOfYear % DARK_GRADIENTS_COUNT;
        return `gradientDark${index}`;
    } else {
        const index = dayOfYear % LIGHT_GRADIENTS_COUNT;
        return `gradientLight${index}`;
    }
}

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    const {colorMode} = useColorMode();
    const isDarkTheme = colorMode === 'dark';

    // Get hash-based gradient class using reactive theme state
    const gradientClass = selectGradientClass(isDarkTheme);
    const selectedGradientClass = styles[gradientClass];

    return (
        <header className={clsx('hero hero--primary', styles.heroBanner, selectedGradientClass)}>
            <div className="container">
                <Heading as="h1" className="hero__title">
                    {siteConfig.title}
                </Heading>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <div className={styles.buttons}>
                    <Link
                        className="button button--secondary button--lg"
                        to="/docs/">
                        <Translate id="Start">Start</Translate>
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default function Home(): ReactNode {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={`Hello from ${siteConfig.title}`}
            description="Description will go into a meta tag in <head />">
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}