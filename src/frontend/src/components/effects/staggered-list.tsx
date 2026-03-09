import * as React from 'react'
import { motion, type Variants } from 'motion/react'
import { cn } from '@/lib/utils'

interface StaggeredListProps {
  children: React.ReactNode
  className?: string
  staggerDelay?: number
}

const containerVariants: Variants = {
  hidden: { opacity: 0 },
  visible: (custom: { staggerDelay: number }) => ({
    opacity: 1,
    transition: {
      staggerChildren: custom.staggerDelay,
    },
  }),
}

const itemVariants: Variants = {
  hidden: (custom: { yOffset: number }) => ({
    opacity: 0,
    y: custom.yOffset,
    filter: 'blur(4px)',
  }),
  visible: (custom: { duration: number }) => ({
    opacity: 1,
    y: 0,
    filter: 'blur(0px)',
    transition: {
      duration: custom.duration,
      ease: [0.4, 0, 0.2, 1],
    },
  }),
}

export function StaggeredList({
  children,
  className,
  staggerDelay = 0.06,
}: StaggeredListProps) {
  // Cap total stagger delay at 0.3s to prevent long waits on large lists
  const childCount = React.Children.count(children)
  const maxTotalDelay = 0.3
  const maxStaggerDelay = childCount > 1 ? maxTotalDelay / (childCount - 1) : staggerDelay
  const cappedDelay = Math.min(staggerDelay, maxStaggerDelay)

  return (
    <motion.div
      className={cn(className)}
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      custom={{ staggerDelay: cappedDelay }}
    >
      {children}
    </motion.div>
  )
}

export function StaggeredItem({
  children,
  className,
  duration = 0.4,
  yOffset = 20,
}: {
  children: React.ReactNode
  className?: string
  duration?: number
  yOffset?: number
}) {
  return (
    <motion.div
      className={cn(className)}
      variants={itemVariants}
      custom={{ duration, yOffset }}
    >
      {children}
    </motion.div>
  )
}
